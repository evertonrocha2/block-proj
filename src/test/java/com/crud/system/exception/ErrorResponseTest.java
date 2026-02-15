package com.crud.system.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe ErrorResponse.
 */
@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    @Test
    @DisplayName("Deve criar ErrorResponse com todos os campos")
    void shouldCreateErrorResponseWithAllFields() {
        LocalDateTime timestamp = LocalDateTime.now();
        List<String> details = Arrays.asList("Erro 1", "Erro 2");
        
        ErrorResponse errorResponse = new ErrorResponse(
            timestamp,
            400,
            "Bad Request",
            "Erro de validação",
            "/api/products"
        );
        errorResponse.setDetails(details);
        
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Erro de validação", errorResponse.getMessage());
        assertEquals("/api/products", errorResponse.getPath());
        assertEquals(details, errorResponse.getDetails());
    }

    @Test
    @DisplayName("Deve criar ErrorResponse sem detalhes")
    void shouldCreateErrorResponseWithoutDetails() {
        LocalDateTime timestamp = LocalDateTime.now();
        
        ErrorResponse errorResponse = new ErrorResponse(
            timestamp,
            404,
            "Not Found",
            "Recurso não encontrado",
            "/api/products/999"
        );
        
        assertNull(errorResponse.getDetails());
    }

    @Test
    @DisplayName("Deve definir e obter todos os campos")
    void shouldSetAndGetAllFields() {
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();
        List<String> details = Arrays.asList("Detalhe 1", "Detalhe 2");
        
        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(500);
        errorResponse.setError("Internal Server Error");
        errorResponse.setMessage("Erro interno");
        errorResponse.setPath("/api/products");
        errorResponse.setDetails(details);
        
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("Erro interno", errorResponse.getMessage());
        assertEquals("/api/products", errorResponse.getPath());
        assertEquals(details, errorResponse.getDetails());
    }

    @Test
    @DisplayName("Deve criar com construtor completo")
    void shouldCreateWithFullConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();
        List<String> details = Arrays.asList("Erro A", "Erro B", "Erro C");
        
        ErrorResponse errorResponse = new ErrorResponse(
            timestamp,
            422,
            "Unprocessable Entity",
            "Regra de negócio violada",
            "/api/products/1",
            details
        );
        
        assertNotNull(errorResponse);
        assertEquals(422, errorResponse.getStatus());
        assertEquals("Unprocessable Entity", errorResponse.getError());
        assertEquals(3, errorResponse.getDetails().size());
    }

    @Test
    @DisplayName("Deve verificar equals e hashCode")
    void shouldCheckEqualsAndHashCode() {
        LocalDateTime timestamp = LocalDateTime.now();
        
        ErrorResponse error1 = new ErrorResponse(
            timestamp,
            400,
            "Bad Request",
            "Mensagem",
            "/api/test"
        );
        
        ErrorResponse error2 = new ErrorResponse(
            timestamp,
            400,
            "Bad Request",
            "Mensagem",
            "/api/test"
        );
        
        ErrorResponse error3 = new ErrorResponse(
            timestamp,
            404,
            "Not Found",
            "Outra mensagem",
            "/api/other"
        );
        
        assertEquals(error1, error2);
        assertEquals(error1.hashCode(), error2.hashCode());
        assertNotEquals(error1, error3);
        assertNotEquals(error1.hashCode(), error3.hashCode());
    }

    @Test
    @DisplayName("Deve gerar toString")
    void shouldGenerateToString() {
        LocalDateTime timestamp = LocalDateTime.now();
        
        ErrorResponse errorResponse = new ErrorResponse(
            timestamp,
            400,
            "Bad Request",
            "Erro de teste",
            "/api/test"
        );
        
        String toString = errorResponse.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("400"));
        assertTrue(toString.contains("Bad Request"));
    }
}
