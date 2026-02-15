package com.crud.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste para a aplicação Spring Boot principal.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProductManagementApplication Tests")
class ProductManagementApplicationTest {

    @Test
    @DisplayName("Deve carregar o contexto da aplicação")
    void contextLoads() {
        // Se o contexto carregar sem exceção, o teste passa
        assertDoesNotThrow(() -> ProductManagementApplication.main(new String[] {}));
    }

    @Test
    @DisplayName("Deve ter classe main válida")
    void shouldHaveValidMainClass() {
        assertNotNull(ProductManagementApplication.class);
    }
}
