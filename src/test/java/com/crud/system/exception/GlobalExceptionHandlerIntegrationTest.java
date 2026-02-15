package com.crud.system.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes integrados para o GlobalExceptionHandler.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("GlobalExceptionHandler Integration Tests")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException")
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/api/products/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() throws Exception {
        String invalidProduct = """
                {
                    "name": "",
                    "description": "",
                    "price": -10.00,
                    "quantity": -5,
                    "category": ""
                }
                """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidProduct))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("Deve tratar BusinessRuleException")
    void shouldHandleBusinessRuleException() throws Exception {
        // Criar primeiro produto
        String product1 = """
                {
                    "name": "Produto Único",
                    "description": "Descrição do produto único",
                    "price": 100.00,
                    "quantity": 10,
                    "category": "Categoria"
                }
                """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(product1))
                .andExpect(status().isCreated());

        // Tentar criar produto com mesmo nome
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(product1))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Business Rule Violation"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException")
    void shouldHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/api/products/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Argument"));
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException")
    void shouldHandleHttpMessageNotReadableException() throws Exception {
        String malformedJson = "{invalid json}";

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed JSON"));
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentTypeMismatchException")
    void shouldHandleMethodArgumentTypeMismatchException() throws Exception {
        mockMvc.perform(get("/api/products/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Parameter Type"));
    }

    @Test
    @DisplayName("Deve retornar estrutura de erro completa")
    void shouldReturnCompleteErrorStructure() throws Exception {
        mockMvc.perform(get("/api/products/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("Deve incluir detalhes de validação")
    void shouldIncludeValidationDetails() throws Exception {
        String invalidProduct = """
                {
                    "name": "AB",
                    "description": "Curto",
                    "price": 0,
                    "quantity": -1,
                    "category": "AB"
                }
                """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidProduct))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Deve tratar NumberFormatException")
    void shouldHandleNumberFormatException() throws Exception {
        // Tenta acessar um produto com ID que causa NumberFormatException
        mockMvc.perform(get("/api/products/99999999999999999999999999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
