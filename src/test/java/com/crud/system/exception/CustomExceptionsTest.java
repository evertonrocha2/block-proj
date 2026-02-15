package com.crud.system.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para as exceções personalizadas.
 */
@DisplayName("Custom Exceptions Tests")
class CustomExceptionsTest {

    @Test
    @DisplayName("Deve criar ResourceNotFoundException com mensagem")
    void shouldCreateResourceNotFoundException() {
        String message = "Produto não encontrado";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Deve criar BusinessRuleException com mensagem")
    void shouldCreateBusinessRuleException() {
        String message = "Violação de regra de negócio";
        BusinessRuleException exception = new BusinessRuleException(message);
        
        assertEquals(message, exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException")
    void shouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Recurso não encontrado");
        });
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException")
    void shouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class, () -> {
            throw new BusinessRuleException("Regra violada");
        });
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException")
    void shouldPropagateResourceNotFoundException() {
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Produto com ID 999 não encontrado");
        });
        
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("Deve propagar BusinessRuleException")
    void shouldPropagateBusinessRuleException() {
        Exception exception = assertThrows(BusinessRuleException.class, () -> {
            throw new BusinessRuleException("Nome duplicado: Notebook Dell");
        });
        
        assertTrue(exception.getMessage().contains("Notebook Dell"));
    }

    @Test
    @DisplayName("Deve criar ResourceNotFoundException com 3 parâmetros")
    void shouldCreateResourceNotFoundExceptionWithThreeParams() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Produto", "id", 999);
        
        assertTrue(exception.getMessage().contains("Produto"));
        assertTrue(exception.getMessage().contains("id"));
        assertTrue(exception.getMessage().contains("999"));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Deve criar BusinessRuleException com mensagem e causa")
    void shouldCreateBusinessRuleExceptionWithCause() {
        String message = "Violação de regra de negócio";
        Throwable cause = new IllegalArgumentException("Invalid state");
        BusinessRuleException exception = new BusinessRuleException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception);
    }
}
