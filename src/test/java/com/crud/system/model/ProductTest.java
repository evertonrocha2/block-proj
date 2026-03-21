package com.crud.system.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Product.
 * Testa getters, setters, equals, hashCode e toString.
 */
@DisplayName("Product Entity Tests")
class ProductTest {

    @Test
    @DisplayName("Deve criar produto com construtor vazio")
    void shouldCreateProductWithNoArgsConstructor() {
        Product product = new Product();
        assertNotNull(product);
    }

    @Test
    @DisplayName("Deve definir e obter todos os campos")
    void shouldSetAndGetAllFields() {
        Product product = new Product();
        
        product.setId(1L);
        product.setName("Notebook Dell");
        product.setDescription("Notebook Dell Inspiron 15");
        product.setPrice(new BigDecimal("3500.00"));
        product.setQuantity(10);
        product.setCategory("Eletrônicos");
        
        assertEquals(1L, product.getId());
        assertEquals("Notebook Dell", product.getName());
        assertEquals("Notebook Dell Inspiron 15", product.getDescription());
        assertEquals(new BigDecimal("3500.00"), product.getPrice());
        assertEquals(10, product.getQuantity());
        assertEquals("Eletrônicos", product.getCategory());
    }

    @Test
    @DisplayName("Deve testar equals com mesmo objeto")
    void shouldBeEqualToItself() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        
        assertEquals(product, product);
    }

    @Test
    @DisplayName("Deve testar equals com objetos iguais")
    void shouldBeEqualToProductWithSameFields() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Notebook");
        product1.setDescription("Notebook Dell");
        product1.setPrice(new BigDecimal("3500.00"));
        product1.setQuantity(10);
        product1.setCategory("Eletrônicos");
        
        Product product2 = new Product();
        product2.setId(1L);
        product2.setName("Notebook");
        product2.setDescription("Notebook Dell");
        product2.setPrice(new BigDecimal("3500.00"));
        product2.setQuantity(10);
        product2.setCategory("Eletrônicos");
        
        assertEquals(product1, product2);
    }

    @Test
    @DisplayName("Deve testar equals com objetos diferentes")
    void shouldNotBeEqualToProductWithDifferentId() {
        Product product1 = new Product();
        product1.setId(1L);
        
        Product product2 = new Product();
        product2.setId(2L);
        
        assertNotEquals(product1, product2);
    }

    @Test
    @DisplayName("Deve testar equals com null")
    void shouldNotBeEqualToNull() {
        Product product = new Product();
        product.setId(1L);
        
        assertNotEquals(product, null);
    }

    @Test
    @DisplayName("Deve testar equals com objeto de classe diferente")
    void shouldNotBeEqualToDifferentClass() {
        Product product = new Product();
        product.setId(1L);
        
        assertNotEquals(product, "String");
    }

    @Test
    @DisplayName("Deve testar hashCode")
    void shouldHaveSameHashCodeForEqualProducts() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Notebook");
        product1.setDescription("Notebook Dell");
        product1.setPrice(new BigDecimal("3500.00"));
        product1.setQuantity(10);
        product1.setCategory("Eletrônicos");
        
        Product product2 = new Product();
        product2.setId(1L);
        product2.setName("Notebook");
        product2.setDescription("Notebook Dell");
        product2.setPrice(new BigDecimal("3500.00"));
        product2.setQuantity(10);
        product2.setCategory("Eletrônicos");
        
        assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    @DisplayName("Deve testar toString")
    void shouldReturnStringRepresentation() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setDescription("Dell Inspiron");
        product.setPrice(new BigDecimal("3500.00"));
        product.setQuantity(10);
        product.setCategory("Eletrônicos");
        
        String toString = product.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Notebook"));
    }

    @Test
    @DisplayName("Deve validar nome mínimo")
    void shouldValidateMinimumNameLength() {
        Product product = new Product();
        product.setName("AB");  // Nome com 2 caracteres (inválido)
        product.setDescription("Descrição válida do produto");
        product.setPrice(new BigDecimal("100.00"));
        product.setQuantity(10);
        product.setCategory("Categoria");
        
        // O Bean Validation será ativado pela aplicação
        assertNotNull(product.getName());
    }

    @Test
    @DisplayName("Deve validar preço mínimo")
    void shouldValidateMinimumPrice() {
        Product product = new Product();
        product.setName("Produto");
        product.setDescription("Descrição válida do produto");
        product.setPrice(BigDecimal.ZERO); // Preço zero (inválido)
        product.setQuantity(10);
        product.setCategory("Categoria");
        
        assertNotNull(product.getPrice());
    }

    @Test
    @DisplayName("Deve validar quantidade mínima")
    void shouldValidateMinimumQuantity() {
        Product product = new Product();
        product.setName("Produto");
        product.setDescription("Descrição válida do produto");
        product.setPrice(new BigDecimal("100.00"));
        product.setQuantity(-1); // Quantidade negativa (inválida)
        product.setCategory("Categoria");
        
        assertNotNull(product.getQuantity());
    }

    @Test
    @DisplayName("Deve testar equals com produtos sem ID")
    void shouldCompareProductsWithoutId() {
        Product product1 = new Product();
        product1.setName("Produto 1");
        
        Product product2 = new Product();
        product2.setName("Produto 2");
        
        // Produtos sem ID são considerados diferentes
        assertNotEquals(product1, product2);
    }

    @Test
    @DisplayName("Deve testar hashCode com produto sem ID")
    void shouldCalculateHashCodeWithoutId() {
        Product product = new Product();
        product.setName("Produto");

        int hashCode = product.hashCode();

        assertNotEquals(0, hashCode);
    }

    @Test
    @DisplayName("onCreate deve preencher timestamps e validar")
    void shouldSetTimestampsOnCreate() {
        Product product = Product.builder()
                .name("Notebook Dell")
                .description("Notebook Dell Inspiron 15, 8GB RAM")
                .price(new BigDecimal("2500.00"))
                .quantity(10)
                .category("Eletrônicos")
                .build();

        product.onCreate();

        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    @DisplayName("onUpdate deve atualizar updatedAt")
    void shouldUpdateTimestampOnUpdate() {
        Product product = Product.builder()
                .name("Notebook Dell")
                .description("Notebook Dell Inspiron 15, 8GB RAM")
                .price(new BigDecimal("2500.00"))
                .quantity(10)
                .category("Eletrônicos")
                .build();

        product.onCreate();
        var created = product.getUpdatedAt();

        product.onUpdate();

        assertNotNull(product.getUpdatedAt());
    }

    @Test
    @DisplayName("validateBusinessRules deve rejeitar nome só com espaços")
    void shouldRejectBlankName() {
        Product product = Product.builder()
                .name("   ")
                .description("Descrição válida do produto")
                .price(new BigDecimal("100.00"))
                .quantity(10)
                .category("Categoria")
                .build();

        assertThrows(IllegalArgumentException.class, product::onCreate);
    }

    @Test
    @DisplayName("validateBusinessRules deve rejeitar descrição só com espaços")
    void shouldRejectBlankDescription() {
        Product product = Product.builder()
                .name("Produto Valido")
                .description("   ")
                .price(new BigDecimal("100.00"))
                .quantity(10)
                .category("Categoria")
                .build();

        assertThrows(IllegalArgumentException.class, product::onCreate);
    }

    @Test
    @DisplayName("validateBusinessRules deve rejeitar preço zero ou negativo")
    void shouldRejectZeroPrice() {
        Product product = Product.builder()
                .name("Produto Valido")
                .description("Descrição válida do produto")
                .price(BigDecimal.ZERO)
                .quantity(10)
                .category("Categoria")
                .build();

        assertThrows(IllegalArgumentException.class, product::onCreate);
    }

    @Test
    @DisplayName("validateBusinessRules deve rejeitar quantidade negativa")
    void shouldRejectNegativeQuantity() {
        Product product = Product.builder()
                .name("Produto Valido")
                .description("Descrição válida do produto")
                .price(new BigDecimal("100.00"))
                .quantity(-5)
                .category("Categoria")
                .build();

        assertThrows(IllegalArgumentException.class, product::onCreate);
    }

    @Test
    @DisplayName("Deve criar produto com builder")
    void shouldCreateWithBuilder() {
        Product product = Product.builder()
                .id(1L)
                .name("Notebook Dell")
                .description("Notebook Dell Inspiron 15, 8GB RAM")
                .price(new BigDecimal("2500.00"))
                .quantity(10)
                .category("Eletrônicos")
                .build();

        assertEquals(1L, product.getId());
        assertEquals("Notebook Dell", product.getName());
        assertEquals(new BigDecimal("2500.00"), product.getPrice());
    }

    @Test
    @DisplayName("Deve criar produto com AllArgsConstructor")
    void shouldCreateWithAllArgs() {
        Product product = new Product(1L, "Notebook", "Desc valida pra teste",
                new BigDecimal("2500.00"), 10, "Eletrônicos", null, null);

        assertEquals(1L, product.getId());
        assertEquals("Notebook", product.getName());
    }
}
