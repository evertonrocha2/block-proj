package com.crud.system.failure;

import com.crud.system.exception.BusinessRuleException;
import com.crud.system.exception.ResourceNotFoundException;
import com.crud.system.model.Product;
import com.crud.system.repository.ProductRepository;
import com.crud.system.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de simulação de falhas.
 * Testa comportamento do sistema em cenários adversos (falhas de rede, timeouts, etc).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service - Failure Simulation Tests")
class ProductFailureSimulationTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product createValidProduct() {
        return Product.builder()
                .id(1L)
                .name("Produto Teste")
                .description("Descrição do produto de teste")
                .price(new BigDecimal("100.00"))
                .quantity(10)
                .category("Teste")
                .build();
    }

    // ========== DATABASE FAILURES ==========

    @Test
    @DisplayName("Deve falhar graciosamente em caso de timeout no banco de dados")
    void shouldFailGracefullyOnDatabaseTimeout() {
        // Arrange
        when(productRepository.findAll())
            .thenThrow(new QueryTimeoutException("Database timeout"));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productService.getAllProducts()
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Erro ao buscar"));
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve falhar graciosamente em violação de integridade")
    void shouldFailGracefullyOnIntegrityViolation() {
        // Arrange
        Product product = createValidProduct();
        when(productRepository.existsByNameAndIdNot(anyString(), isNull())).thenReturn(false);
        when(productRepository.save(any()))
            .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productService.createProduct(product)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Erro ao criar"));
    }

    @Test
    @DisplayName("Deve falhar graciosamente em erro de acesso ao banco")
    void shouldFailGracefullyOnDataAccessException() {
        // Arrange
        when(productRepository.findById(1L))
            .thenThrow(new DataAccessException("Connection refused") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productService.getProductById(1L)
        );

        assertNotNull(exception.getMessage());
    }

    // ========== NETWORK FAILURES ==========

    @Test
    @DisplayName("Deve falhar graciosamente em falha de conexão")
    void shouldFailGracefullyOnConnectionFailure() {
        // Arrange
        Product product = createValidProduct();
        when(productRepository.existsByNameAndIdNot(anyString(), isNull())).thenReturn(false);
        when(productRepository.save(any()))
            .thenThrow(new DataAccessException("Network error") {});

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
            () -> productService.createProduct(product));
    }

    // ========== RESOURCE NOT FOUND ==========

    @Test
    @DisplayName("Deve lançar exceção apropriada quando produto não existe")
    void shouldThrowAppropriateExceptionWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.getProductById(999L)
        );

        assertTrue(exception.getMessage().contains("não encontrado"));
        verify(productRepository, times(1)).findById(999L);
    }

    // ========== CONCURRENT MODIFICATION ==========

    @Test
    @DisplayName("Deve detectar modificação concorrente")
    void shouldDetectConcurrentModification() {
        // Arrange
        Product product = createValidProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        lenient().when(productRepository.existsByNameAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(productRepository.save(any()))
            .thenThrow(new DataAccessException("Optimistic locking failure") {});

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
            () -> productService.updateProduct(1L, product));
    }

    // ========== FAIL EARLY TESTS ==========

    @Test
    @DisplayName("Deve falhar cedo com ID inválido")
    void shouldFailEarlyWithInvalidId() {
        // Não deve nem tentar acessar o repositório
        assertThrows(IllegalArgumentException.class, 
            () -> productService.getProductById(0L));
        
        assertThrows(IllegalArgumentException.class, 
            () -> productService.getProductById(-1L));

        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Deve falhar cedo com categoria vazia")
    void shouldFailEarlyWithEmptyCategory() {
        assertThrows(IllegalArgumentException.class, 
            () -> productService.getProductsByCategory(""));
        
        assertThrows(IllegalArgumentException.class, 
            () -> productService.getProductsByCategory("   "));

        verify(productRepository, never()).findByCategory(anyString());
    }

    @Test
    @DisplayName("Deve falhar cedo com nome de busca vazio")
    void shouldFailEarlyWithEmptySearchName() {
        assertThrows(IllegalArgumentException.class, 
            () -> productService.searchProductsByName(""));
        
        assertThrows(IllegalArgumentException.class, 
            () -> productService.searchProductsByName("   "));

        verify(productRepository, never()).searchByName(anyString());
    }

    // ========== TRANSACTION ROLLBACK ==========

    @Test
    @DisplayName("Deve reverter transação em caso de erro")
    void shouldRollbackTransactionOnError() {
        // Arrange
        Product product = createValidProduct();
        when(productRepository.existsByNameAndIdNot(anyString(), isNull())).thenReturn(false);
        when(productRepository.save(any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> productService.createProduct(product));

        // Verifica que save foi chamado apenas uma vez (não houve retry)
        verify(productRepository, times(1)).save(any());
    }

    // ========== DUPLICATE PREVENTION ==========

    @Test
    @DisplayName("Deve prevenir criação de duplicatas")
    void shouldPreventDuplicateCreation() {
        // Arrange
        Product product = createValidProduct();
        when(productRepository.existsByNameAndIdNot(anyString(), isNull())).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productService.createProduct(product)
        );

        assertTrue(exception.getMessage().contains("Já existe"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve prevenir atualização com nome duplicado")
    void shouldPreventUpdateWithDuplicateName() {
        // Arrange
        Product existing = createValidProduct();
        existing.setId(1L);
        existing.setName("Produto Original");

        Product update = createValidProduct();
        update.setName("Produto Duplicado");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByNameAndIdNot("Produto Duplicado", 1L)).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productService.updateProduct(1L, update)
        );

        assertTrue(exception.getMessage().contains("Já existe"));
        verify(productRepository, never()).save(any());
    }

    // ========== NULL SAFETY ==========

    @Test
    @DisplayName("Deve proteger contra argumentos nulos")
    void shouldProtectAgainstNullArguments() {
        assertThrows(Exception.class, 
            () -> productService.createProduct(null));
        
        assertThrows(Exception.class, 
            () -> productService.getProductById(null));
        
        assertThrows(Exception.class, 
            () -> productService.updateProduct(1L, null));
        
        assertThrows(Exception.class, 
            () -> productService.deleteProduct(null));
    }

    // ========== ERROR MESSAGE SAFETY ==========

    @Test
    @DisplayName("Mensagens de erro não devem expor informações sensíveis")
    void errorMessagesShouldNotExposeSensitiveInfo() {
        // Arrange
        when(productRepository.findById(1L))
            .thenThrow(new DataAccessException("Connection to database 192.168.1.100:5432 failed") {});

        // Act
        try {
            productService.getProductById(1L);
            fail("Should have thrown exception");
        } catch (BusinessRuleException e) {
            // Assert - Mensagem genérica, sem detalhes de infraestrutura
            assertFalse(e.getMessage().contains("192.168"));
            assertFalse(e.getMessage().contains("5432"));
            assertFalse(e.getMessage().contains("Connection"));
            assertTrue(e.getMessage().contains("Erro ao buscar"));
        }
    }
}
