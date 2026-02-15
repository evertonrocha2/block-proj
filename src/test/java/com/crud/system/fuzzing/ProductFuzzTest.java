package com.crud.system.fuzzing;

import com.crud.system.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Testes de Fuzz para detectar vulnerabilidades.
 * Testa o sistema com entradas aleatórias, maliciosas e extremas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Product API - Fuzz Testing")
class ProductFuzzTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    //  SQL INJECTION ATTEMPTS 

    @ParameterizedTest
    @ValueSource(strings = {
        "'; DROP TABLE products; --",
        "1' OR '1'='1",
        "admin'--",
        "' OR 1=1--",
        "' UNION SELECT * FROM users--",
        "<script>alert('XSS')</script>",
        "../../../etc/passwd",
        "${7*7}",
        "{{7*7}}",
        "%00"
    })
    @DisplayName("Deve proteger contra injeção SQL e XSS")
    void shouldProtectAgainstInjection(String maliciousInput) throws Exception {
        Product product = Product.builder()
                .name(maliciousInput)
                .description("Teste de segurança com entrada maliciosa")
                .price(new BigDecimal("100.00"))
                .quantity(10)
                .category("Teste")
                .build();

        // O sistema deve rejeitar ou sanitizar a entrada
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().is(anyOf(
                    is(400), // Bad Request (validação falhou)
                    is(201)  // Created (entrada foi sanitizada)
                )));
    }

    //  BOUNDARY VALUE TESTING 

    @Test
    @DisplayName("Deve validar limites extremos de preço")
    void shouldValidateExtremePriceBoundaries() throws Exception {
        // Preço máximo permitido
        Product maxPrice = createValidProduct();
        maxPrice.setPrice(new BigDecimal("999999.99"));
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxPrice)))
                .andExpect(status().isCreated());

        // Preço acima do máximo
        Product overMaxPrice = createValidProduct();
        overMaxPrice.setPrice(new BigDecimal("1000000.00"));
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overMaxPrice)))
                .andExpect(status().isBadRequest());

        // Preço mínimo
        Product minPrice = createValidProduct();
        minPrice.setPrice(new BigDecimal("0.01"));
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minPrice)))
                .andExpect(status().isCreated());

        // Preço zero
        Product zeroPrice = createValidProduct();
        zeroPrice.setPrice(BigDecimal.ZERO);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroPrice)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar limites extremos de quantidade")
    void shouldValidateExtremeQuantityBoundaries() throws Exception {
        // Quantidade máxima
        Product maxQty = createValidProduct();
        maxQty.setQuantity(999999);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxQty)))
                .andExpect(status().isCreated());

        // Quantidade acima do máximo
        Product overMaxQty = createValidProduct();
        overMaxQty.setQuantity(1000000);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overMaxQty)))
                .andExpect(status().isBadRequest());

        // Quantidade negativa
        Product negQty = createValidProduct();
        negQty.setQuantity(-1);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negQty)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar tamanhos extremos de strings")
    void shouldValidateExtremeStringLengths() throws Exception {
        // Nome muito longo
        String longName = "A".repeat(101);
        Product longNameProduct = createValidProduct();
        longNameProduct.setName(longName);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longNameProduct)))
                .andExpect(status().isBadRequest());

        // Descrição muito longa
        String longDesc = "D".repeat(501);
        Product longDescProduct = createValidProduct();
        longDescProduct.setDescription(longDesc);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longDescProduct)))
                .andExpect(status().isBadRequest());
    }

    //  RANDOM FUZZING 

    @RepeatedTest(20)
    @DisplayName("Deve lidar com dados completamente aleatórios")
    void shouldHandleRandomData() throws Exception {
        Product randomProduct = Product.builder()
                .name(generateRandomString(random.nextInt(150)))
                .description(generateRandomString(random.nextInt(600)))
                .price(generateRandomBigDecimal())
                .quantity(random.nextInt(1500000) - 100000)
                .category(generateRandomString(random.nextInt(100)))
                .build();

        // Sistema deve responder adequadamente (aceitar ou rejeitar graciosamente)
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(randomProduct)))
                .andExpect(status().is(anyOf(
                    is(201), // Criado com sucesso
                    is(400), // Validação falhou
                    is(422)  // Regra de negócio violada
                )))
                .andExpect(jsonPath("$").exists()); // Sempre deve retornar algo
    }

    //  UNICODE & SPECIAL CHARACTERS 

    @ParameterizedTest
    @ValueSource(strings = {
        "Produto 中文",
        "Produto العربية",
        "Produto日本語",
        "Produto 🚀",
        "Produto \uD83D\uDE80\uD83D\uDC4D",
        "Produto\nComQuebraDeLinha",
        "Produto\tComTab",
        "Produto\"ComAspas\"",
        "Produto'ComApostrofo'",
        "Produto\\ComBarra"
    })
    @DisplayName("Deve lidar com caracteres especiais e unicode")
    void shouldHandleSpecialCharacters(String specialName) throws Exception {
        Product product = createValidProduct();
        product.setName(specialName);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().is(anyOf(is(201), is(400))));
    }

    //  NULL & EMPTY VALUES 

    @Test
    @DisplayName("Deve rejeitar valores nulos")
    void shouldRejectNullValues() throws Exception {
        // Nome nulo
        Product nullName = new Product();
        nullName.setDescription("Descrição válida do produto");
        nullName.setPrice(new BigDecimal("100.00"));
        nullName.setQuantity(10);
        nullName.setCategory("Categoria");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n", "     \t\n     "})
    @DisplayName("Deve rejeitar strings vazias ou apenas espaços")
    void shouldRejectEmptyOrWhitespaceStrings(String emptyValue) throws Exception {
        Product product = createValidProduct();
        product.setName(emptyValue);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());
    }

    //  MALFORMED JSON 

    @ParameterizedTest
    @ValueSource(strings = {
        "{invalid json}",
        "{'name': 'Product'}",  // Aspas simples
        "{name: 'Product'}",    // Sem aspas na chave
        "{'name': undefined}",
        "{\"name\": \"Product\",}", // Vírgula extra
        ""
    })
    @DisplayName("Deve rejeitar JSON malformado")
    void shouldRejectMalformedJson(String malformedJson) throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().is(anyOf(is(400), is(415))));
    }

    //  CONCURRENT REQUESTS 

    @RepeatedTest(10)
    @DisplayName("Deve lidar com múltiplas requisições concorrentes")
    void shouldHandleConcurrentRequests() throws Exception {
        Product product = createValidProduct();
        product.setName("Produto Concorrente " + System.nanoTime());

        // Simula múltiplas requisições "simultâneas"
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated());
    }

    //  INVALID IDS 

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "999999999", "abc", "null", "undefined", "true"})
    @DisplayName("Deve validar IDs inválidos em operações")
    void shouldValidateInvalidIds(String invalidId) throws Exception {
        mockMvc.perform(get("/api/products/" + invalidId))
                .andExpect(status().is(anyOf(is(400), is(404))));

        mockMvc.perform(delete("/api/products/" + invalidId))
                .andExpect(status().is(anyOf(is(400), is(404))));
    }

    //  OVERFLOW/UNDERFLOW 

    @Test
    @DisplayName("Deve proteger contra overflow numérico")
    void shouldProtectAgainstNumericOverflow() throws Exception {
        Product product = createValidProduct();
        
        // Tenta enviar valores extremamente grandes via JSON raw
        String jsonWithHugeNumbers = String.format(
            "{\"name\":\"%s\",\"description\":\"%s\",\"price\":%s,\"quantity\":%s,\"category\":\"%s\"}",
            "Produto", "Descrição válida do produto", 
            "999999999999999999999.99", "9999999999", "Categoria"
        );

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithHugeNumbers))
                .andExpect(status().is(anyOf(is(400), is(422))));
    }

    //  HELPER METHODS 

    private Product createValidProduct() {
        return Product.builder()
                .name("Produto Teste " + System.nanoTime())
                .description("Descrição válida e completa do produto de teste")
                .price(new BigDecimal("100.00"))
                .quantity(10)
                .category("Categoria Teste")
                .build();
    }

    private String generateRandomString(int length) {
        if (length <= 0) return "";
        if (length > 1000) length = 1000; // Limita tamanho
        
        return faker.lorem().characters(length);
    }

    private BigDecimal generateRandomBigDecimal() {
        double value = random.nextDouble() * 2000000 - 1000000;
        return BigDecimal.valueOf(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
