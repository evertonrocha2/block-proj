package com.crud.system.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler - Testes Unitários")
class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Deve tratar ConstraintViolationException com múltiplas violações")
    void shouldHandleConstraintViolationException() {
        // Cria violações mockadas
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("name");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("não pode ser vazio");
        violations.add(violation1);
        
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("price");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("deve ser maior que zero");
        violations.add(violation2);
        
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // Testa o handler
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Violação de Constraint", response.getBody().getError());
        assertNotNull(response.getBody().getDetails());
        assertEquals(2, response.getBody().getDetails().size());
    }

    @Test
    @DisplayName("Deve tratar NumberFormatException")
    void shouldHandleNumberFormatException() {
        NumberFormatException exception = new NumberFormatException("Invalid number format");

        ResponseEntity<ErrorResponse> response = handler.handleNumberFormatException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Formato Numérico Inválido", response.getBody().getError());
        assertEquals("O formato do número fornecido é inválido", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException")
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Produto não encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Não Encontrado", response.getBody().getError());
    }

    @Test
    @DisplayName("Deve tratar BusinessRuleException")
    void shouldHandleBusinessRuleException() {
        BusinessRuleException exception = new BusinessRuleException("Regra de negócio violada");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessRuleException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Violação de Regra de Negócio", response.getBody().getError());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Argumento Inválido", response.getBody().getError());
    }

    @Test
    @DisplayName("Deve tratar Exception genérica sem expor detalhes")
    void shouldHandleGenericException() {
        Exception exception = new RuntimeException("Erro interno do sistema");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Erro Interno do Servidor", response.getBody().getError());
        // Verifica que não expõe detalhes internos
        assertFalse(response.getBody().getMessage().contains("RuntimeException"));
        assertTrue(response.getBody().getMessage().contains("erro inesperado"));
    }
}
