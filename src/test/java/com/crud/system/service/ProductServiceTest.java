package com.crud.system.service;

import com.crud.system.exception.BusinessRuleException;
import com.crud.system.exception.ResourceNotFoundException;
import com.crud.system.model.Product;
import com.crud.system.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProductService.
 * Cobre todos os caminhos de código, incluindo cenários de falha.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service - Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product validProduct;

    @BeforeEach
    void setUp() {
        validProduct = Product.builder()
                .id(1L)
                .name("Notebook Dell")
                .description("Notebook Dell Inspiron 15, 8GB RAM, 256GB SSD")
                .price(new BigDecimal("2500.00"))
                .quantity(10)
                .category("Eletrônicos")
                .build();
    }

    //  CREATE TESTS 

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void shouldCreateProductSuccessfully() {
        // Arrange
        when(productRepository.existsByNameAndIdNot(anyString(), isNull())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(validProduct);

        // Act
        Product result = productService.createProduct(validProduct);

        // Assert
        assertNotNull(result);
        assertEquals(validProduct.getName(), result.getName());
        verify(productRepository).existsByNameAndIdNot(validProduct.getName(), null);
        verify(productRepository).save(validProduct);
    }

    @Test
    @DisplayName("Deve falhar ao criar produto com nome duplicado")
    void shouldFailToCreateProductWithDuplicateName() {
        // Arrange
        when(productRepository.existsByNameAndIdNot(anyString(), isNull())).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productService.createProduct(validProduct)
        );
        
        assertTrue(exception.getMessage().contains("Já existe um produto"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao criar produto com erro no banco de dados")
    void shouldFailToCreateProductWithDatabaseError() {
        // Arrange
        when(productRepository.existsByNameAndIdNot(anyString(), isNull())).thenReturn(false);
        when(productRepository.save(any())).thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productService.createProduct(validProduct)
        );
        
        assertTrue(exception.getMessage().contains("Erro ao criar produto"));
    }

    //  READ TESTS 

    @Test
    @DisplayName("Deve retornar todos os produtos")
    void shouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(validProduct, 
            Product.builder()
                .id(2L)
                .name("Mouse Logitech")
                .description("Mouse sem fio Logitech MX Master")
                .price(new BigDecimal("350.00"))
                .quantity(25)
                .category("Periféricos")
                .build()
        );
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void shouldReturnEmptyListWhenNoProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void shouldGetProductByIdSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(validProduct.getId(), result.getId());
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto inexistente")
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.getProductById(999L)
        );
        
        assertTrue(exception.getMessage().contains("não encontrado"));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    @DisplayName("Deve falhar ao buscar produto com ID inválido")
    void shouldFailToGetProductWithInvalidId(Long invalidId) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            productService.getProductById(invalidId)
        );
        
        verify(productRepository, never()).findById(anyLong());
    }

    //  UPDATE TESTS 

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void shouldUpdateProductSuccessfully() {
        // Arrange
        Product updatedData = Product.builder()
                .name("Notebook Dell Atualizado")
                .description("Nova descrição do produto")
                .price(new BigDecimal("2600.00"))
                .quantity(15)
                .category("Eletrônicos")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
        when(productRepository.existsByNameAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(validProduct);

        // Act
        Product result = productService.updateProduct(1L, updatedData);

        // Assert
        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve falhar ao atualizar com nome duplicado")
    void shouldFailToUpdateWithDuplicateName() {
        // Arrange
        Product updatedData = Product.builder()
                .name("Mouse Logitech")
                .description("Nova descrição")
                .price(new BigDecimal("2600.00"))
                .quantity(15)
                .category("Eletrônicos")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
        when(productRepository.existsByNameAndIdNot("Mouse Logitech", 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> 
            productService.updateProduct(1L, updatedData)
        );
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao atualizar produto inexistente")
    void shouldFailToUpdateNonExistentProduct() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            productService.updateProduct(999L, validProduct)
        );
    }

    //  DELETE TESTS 

    @Test
    @DisplayName("Deve deletar produto com sucesso")
    void shouldDeleteProductSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
        doNothing().when(productRepository).delete(validProduct);

        // Act
        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        // Assert
        verify(productRepository).delete(validProduct);
    }

    @Test
    @DisplayName("Deve falhar ao deletar produto inexistente")
    void shouldFailToDeleteNonExistentProduct() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            productService.deleteProduct(999L)
        );
        
        verify(productRepository, never()).delete(any());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -50})
    @DisplayName("Deve falhar ao deletar com ID inválido")
    void shouldFailToDeleteWithInvalidId(Long invalidId) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            productService.deleteProduct(invalidId)
        );
    }

    //  SEARCH TESTS

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void shouldSearchProductsByCategory() {
        // Arrange
        List<Product> products = Arrays.asList(validProduct);
        when(productRepository.findByCategory("Eletrônicos")).thenReturn(products);

        // Act
        List<Product> result = productService.getProductsByCategory("Eletrônicos");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByCategory("Eletrônicos");
    }

    @ParameterizedTest
    @CsvSource({
        "'', Categoria não pode ser vazia",
        "'   ', Categoria não pode ser vazia"
    })
    @DisplayName("Deve falhar ao buscar com categoria inválida")
    void shouldFailToSearchWithInvalidCategory(String category, String expectedMessage) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.getProductsByCategory(category)
        );
        
        assertTrue(exception.getMessage().contains("Categoria"));
    }

    @Test
    @DisplayName("Deve buscar produtos por nome")
    void shouldSearchProductsByName() {
        // Arrange
        List<Product> products = Arrays.asList(validProduct);
        when(productRepository.searchByName("Notebook")).thenReturn(products);

        // Act
        List<Product> result = productService.searchProductsByName("Notebook");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve buscar produtos com estoque baixo")
    void shouldGetLowStockProducts() {
        // Arrange
        Product lowStockProduct = Product.builder()
                .id(2L)
                .name("Produto com estoque baixo")
                .description("Descrição do produto")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .category("Diversos")
                .build();
        
        when(productRepository.findLowStockProducts(10)).thenReturn(Arrays.asList(lowStockProduct));

        // Act
        List<Product> result = productService.getLowStockProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getQuantity() < 10);
    }

    @Test
    @DisplayName("Deve tratar erro ao buscar produtos com estoque baixo")
    void shouldHandleErrorWhenGettingLowStockProducts() {
        // Arrange
        when(productRepository.findLowStockProducts(anyInt()))
            .thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> 
            productService.getLowStockProducts()
        );
    }
}
