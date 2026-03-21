package com.crud.system.integration;

import com.crud.system.model.Product;
import com.crud.system.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Product API - Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        
        testProduct = Product.builder()
                .name("Notebook Dell Integration Test")
                .description("Notebook para testes de integração")
                .price(new BigDecimal("3000.00"))
                .quantity(20)
                .category("Eletrônicos")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Cenário completo: Criar, Ler, Atualizar e Deletar produto")
    void shouldPerformFullCRUDCycle() throws Exception {
        // 1. CREATE - Criar produto
        MvcResult createResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(testProduct.getName()))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Product createdProduct = objectMapper.readValue(responseJson, Product.class);
        Long productId = createdProduct.getId();
        assertNotNull(productId);

        // 2. READ - Buscar produto criado
        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value(testProduct.getName()));

        // 3. UPDATE - Atualizar produto
        Product updatedProduct = Product.builder()
                .name("Notebook Dell ATUALIZADO")
                .description("Descrição atualizada")
                .price(new BigDecimal("3200.00"))
                .quantity(25)
                .category("Eletrônicos")
                .build();

        mockMvc.perform(put("/api/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Dell ATUALIZADO"))
                .andExpect(jsonPath("$.price").value(3200.00))
                .andExpect(jsonPath("$.quantity").value(25));

        // 4. DELETE - Deletar produto
        mockMvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isNoContent());

        // 5. VERIFY - Confirmar que foi deletado
        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    @DisplayName("Deve falhar ao criar produto com nome duplicado")
    void shouldFailToCreateDuplicateProduct() throws Exception {
        // Criar primeiro produto
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated());

        // Tentar criar produto com mesmo nome
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Violação de Regra de Negócio"))
                .andExpect(jsonPath("$.message", containsString("Já existe")));
    }

    @Test
    @Order(3)
    @DisplayName("Deve validar campos obrigatórios")
    void shouldValidateRequiredFields() throws Exception {
        Product invalidProduct = new Product();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erro de Validação"))
                .andExpect(jsonPath("$.details", not(empty())));
    }

    @Test
    @Order(4)
    @DisplayName("Deve buscar produtos por categoria")
    void shouldSearchByCategory() throws Exception {
        // Criar produtos de diferentes categorias
        Product product1 = Product.builder()
                .name("Mouse Logitech")
                .description("Mouse sem fio")
                .price(new BigDecimal("150.00"))
                .quantity(30)
                .category("Periféricos")
                .build();

        Product product2 = Product.builder()
                .name("Teclado Mecânico")
                .description("Teclado RGB")
                .price(new BigDecimal("350.00"))
                .quantity(15)
                .category("Periféricos")
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product2)))
                .andExpect(status().isCreated());

        // Buscar por categoria
        mockMvc.perform(get("/api/products/category/Periféricos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("Periféricos"))));
    }

    @Test
    @Order(5)
    @DisplayName("Deve buscar produtos por nome")
    void shouldSearchByName() throws Exception {
        // Criar produtos
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated());

        // Buscar por nome
        mockMvc.perform(get("/api/products/search")
                .param("name", "Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].name", containsString("Notebook")));
    }

    @Test
    @Order(6)
    @DisplayName("Deve retornar produtos com estoque baixo")
    void shouldReturnLowStockProducts() throws Exception {
        // Criar produto com estoque baixo
        Product lowStockProduct = Product.builder()
                .name("Produto Estoque Baixo")
                .description("Este produto tem estoque baixo")
                .price(new BigDecimal("100.00"))
                .quantity(5) // Abaixo do threshold de 10
                .category("Diversos")
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lowStockProduct)))
                .andExpect(status().isCreated());

        // Buscar produtos com estoque baixo
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].quantity", lessThan(10)));
    }

    @Test
    @Order(7)
    @DisplayName("Deve falhar ao atualizar produto inexistente")
    void shouldFailToUpdateNonExistentProduct() throws Exception {
        mockMvc.perform(put("/api/products/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }

    @Test
    @Order(8)
    @DisplayName("Deve falhar ao deletar produto inexistente")
    void shouldFailToDeleteNonExistentProduct() throws Exception {
        mockMvc.perform(delete("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }

    @Test
    @Order(9)
    @DisplayName("Deve validar limites de preço")
    void shouldValidatePriceLimits() throws Exception {
        // Preço muito alto
        Product expensiveProduct = Product.builder()
                .name("Produto Caro")
                .description("Este produto é muito caro")
                .price(new BigDecimal("9999999.99"))
                .quantity(1)
                .category("Luxo")
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expensiveProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("Deve validar quantidade negativa")
    void shouldValidateNegativeQuantity() throws Exception {
        Product negativeQtyProduct = Product.builder()
                .name("Produto Quantidade Negativa")
                .description("Este produto tem quantidade negativa")
                .price(new BigDecimal("100.00"))
                .quantity(-5)
                .category("Teste")
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeQtyProduct)))
                .andExpect(status().isBadRequest());
    }
}
