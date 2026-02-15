package com.crud.system.controller;

import com.crud.system.model.Product;
import com.crud.system.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Testes do Controller usando MockMvc.
 * Testa a camada HTTP sem inicializar todo o contexto Spring.
 */
@WebMvcTest(ProductController.class)
@DisplayName("Product Controller - Unit Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired     
    private ObjectMapper objectMapper;

    @MockBean
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

    @Test
    @DisplayName("POST /api/products - Deve criar produto com sucesso")
    void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        when(productService.createProduct(ArgumentMatchers.any(Product.class))).thenReturn(validProduct);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Notebook Dell"))
                .andExpect(jsonPath("$.price").value(2500.00));

        verify(productService).createProduct(ArgumentMatchers.any(Product.class));
    }

    @Test
    @DisplayName("POST /api/products - Deve falhar com dados inválidos")
    void shouldFailToCreateProductWithInvalidData() throws Exception {
        // Arrange
        Product invalidProduct = Product.builder()
                .name("AB") // Nome muito curto
                .description("Desc") // Descrição muito curta
                .price(new BigDecimal("-10.00")) // Preço negativo
                .quantity(-5) // Quantidade negativa
                .category("AB") // Categoria muito curta
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("GET /api/products - Deve retornar todos os produtos")
    void shouldGetAllProducts() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Arrays.asList(validProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Notebook Dell"));

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/products - Deve retornar lista vazia")
    void shouldReturnEmptyList() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Deve retornar produto por ID")
    void shouldGetProductById() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(validProduct);

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Notebook Dell"));

        verify(productService).getProductById(1L);
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Deve atualizar produto")
    void shouldUpdateProduct() throws Exception {
        // Arrange
        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Notebook Dell Atualizado")
                .description("Nova descrição do produto")
                .price(new BigDecimal("2600.00"))
                .quantity(15)
                .category("Eletrônicos")
                .build();

        when(productService.updateProduct(eq(1L), ArgumentMatchers.any(Product.class))).thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Dell Atualizado"))
                .andExpect(jsonPath("$.price").value(2600.00));

        verify(productService).updateProduct(eq(1L), ArgumentMatchers.any(Product.class));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Deve deletar produto")
    void shouldDeleteProduct() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    @DisplayName("GET /api/products/search - Deve buscar por nome")
    void shouldSearchByName() throws Exception {
        // Arrange
        when(productService.searchProductsByName("Notebook")).thenReturn(Arrays.asList(validProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("name", "Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Notebook Dell"));

        verify(productService).searchProductsByName("Notebook");
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - Deve buscar por categoria")
    void shouldSearchByCategory() throws Exception {
        // Arrange
        when(productService.getProductsByCategory("Eletrônicos")).thenReturn(Arrays.asList(validProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products/category/Eletrônicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("Eletrônicos"));

        verify(productService).getProductsByCategory("Eletrônicos");
    }

    @Test
    @DisplayName("GET /api/products/low-stock - Deve retornar produtos com estoque baixo")
    void shouldGetLowStockProducts() throws Exception {
        // Arrange
        Product lowStock = Product.builder()
                .id(2L)
                .name("Produto Estoque Baixo")
                .description("Produto com quantidade baixa")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .category("Diversos")
                .build();

        when(productService.getLowStockProducts()).thenReturn(Arrays.asList(lowStock));

        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantity").value(5));

        verify(productService).getLowStockProducts();
    }
}
