package com.crud.system.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para as entidades Order, OrderItem e o enum OrderStatus.
 */
@DisplayName("Order, OrderItem e OrderStatus Tests")
class OrderTest {

    private Product criarProduto(Long id, String nome, BigDecimal preco) {
        return Product.builder()
                .id(id)
                .name(nome)
                .price(preco)
                .build();
    }

    private OrderItem criarItem(Long id, Product produto, int quantidade, BigDecimal precoUnitario) {
        BigDecimal subtotal = precoUnitario.multiply(new BigDecimal(quantidade));
        return OrderItem.builder()
                .id(id)
                .product(produto)
                .quantity(quantidade)
                .unitPrice(precoUnitario)
                .subtotal(subtotal)
                .build();
    }

    @Nested
    @DisplayName("Order - calculateTotal")
    class CalculateTotalTests {

        @Test
        @DisplayName("Deve calcular total com múltiplos itens")
        void deveCalcularTotalComMultiplosItens() {
            Order order = new Order();
            order.setItems(new ArrayList<>());

            Product p1 = criarProduto(1L, "Caneta", new BigDecimal("2.50"));
            Product p2 = criarProduto(2L, "Caderno", new BigDecimal("15.00"));

            OrderItem item1 = criarItem(1L, p1, 4, new BigDecimal("2.50"));
            OrderItem item2 = criarItem(2L, p2, 2, new BigDecimal("15.00"));

            order.getItems().add(item1);
            order.getItems().add(item2);

            order.calculateTotal();

            // 4*2.50 + 2*15.00 = 10.00 + 30.00 = 40.00
            assertEquals(new BigDecimal("40.00"), order.getTotalValue());
        }

        @Test
        @DisplayName("Deve retornar zero quando lista de itens estiver vazia")
        void deveRetornarZeroComListaVazia() {
            Order order = new Order();
            order.setItems(new ArrayList<>());

            order.calculateTotal();

            assertEquals(BigDecimal.ZERO, order.getTotalValue());
        }

        @Test
        @DisplayName("Deve retornar zero quando items for null")
        void deveRetornarZeroComItemsNull() {
            Order order = new Order();
            order.setItems(null);

            order.calculateTotal();

            assertEquals(BigDecimal.ZERO, order.getTotalValue());
        }
    }

    @Nested
    @DisplayName("Order - addItem e removeItem")
    class AddRemoveItemTests {

        @Test
        @DisplayName("Deve adicionar item e recalcular total")
        void deveAdicionarItemERecalcularTotal() {
            Order order = new Order();
            order.setItems(new ArrayList<>());

            Product produto = criarProduto(1L, "Borracha", new BigDecimal("1.50"));
            OrderItem item = criarItem(1L, produto, 3, new BigDecimal("1.50"));

            order.addItem(item);

            assertEquals(1, order.getItems().size());
            assertEquals(order, item.getOrder());
            assertEquals(new BigDecimal("4.50"), order.getTotalValue());
        }

        @Test
        @DisplayName("Deve adicionar item quando lista interna for null")
        void deveAdicionarItemComListaNull() {
            Order order = new Order();
            order.setItems(null);

            Product produto = criarProduto(1L, "Lápis", new BigDecimal("0.75"));
            OrderItem item = criarItem(1L, produto, 10, new BigDecimal("0.75"));

            order.addItem(item);

            assertNotNull(order.getItems());
            assertEquals(1, order.getItems().size());
        }

        @Test
        @DisplayName("Deve remover item e recalcular total")
        void deveRemoverItemERecalcularTotal() {
            Order order = new Order();
            order.setItems(new ArrayList<>());

            Product p1 = criarProduto(1L, "Apontador", new BigDecimal("3.00"));
            Product p2 = criarProduto(2L, "Régua", new BigDecimal("5.00"));

            OrderItem item1 = criarItem(1L, p1, 2, new BigDecimal("3.00"));
            OrderItem item2 = criarItem(2L, p2, 1, new BigDecimal("5.00"));

            order.addItem(item1);
            order.addItem(item2);

            // Total: 6.00 + 5.00 = 11.00
            assertEquals(new BigDecimal("11.00"), order.getTotalValue());

            order.removeItem(item1);

            assertEquals(1, order.getItems().size());
            assertNull(item1.getOrder());
            assertEquals(new BigDecimal("5.00"), order.getTotalValue());
        }

        @Test
        @DisplayName("Deve ignorar removeItem quando items for null")
        void deveIgnorarRemoveItemComListaNull() {
            Order order = new Order();
            order.setItems(null);

            OrderItem item = new OrderItem();

            assertDoesNotThrow(() -> order.removeItem(item));
        }
    }

    @Nested
    @DisplayName("Order - Getters e Setters")
    class OrderGettersSettersTests {

        @Test
        @DisplayName("Deve definir e obter todos os campos da Order")
        void deveDefinirEObterTodosOsCampos() {
            LocalDateTime agora = LocalDateTime.now();
            Order order = new Order();

            order.setId(42L);
            order.setClientName("Rafaela Mendes");
            order.setClientEmail("rafaela@email.com");
            order.setStatus(OrderStatus.PENDING);
            order.setTotalValue(new BigDecimal("150.00"));
            order.setItems(new ArrayList<>());
            order.setCreatedAt(agora);
            order.setUpdatedAt(agora);

            assertEquals(42L, order.getId());
            assertEquals("Rafaela Mendes", order.getClientName());
            assertEquals("rafaela@email.com", order.getClientEmail());
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(new BigDecimal("150.00"), order.getTotalValue());
            assertTrue(order.getItems().isEmpty());
            assertEquals(agora, order.getCreatedAt());
            assertEquals(agora, order.getUpdatedAt());
        }

        @Test
        @DisplayName("Deve criar Order via builder")
        void deveCriarOrderViaBuilder() {
            Order order = Order.builder()
                    .id(1L)
                    .clientName("Lucas")
                    .clientEmail("lucas@email.com")
                    .status(OrderStatus.CONFIRMED)
                    .totalValue(new BigDecimal("300.00"))
                    .build();

            assertEquals(1L, order.getId());
            assertEquals("Lucas", order.getClientName());
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        }

        @Test
        @DisplayName("Deve testar equals por ID")
        void deveTestarEqualsPorId() {
            Order o1 = new Order();
            o1.setId(1L);
            o1.setClientName("Alice");

            Order o2 = new Order();
            o2.setId(1L);
            o2.setClientName("Bob");

            assertEquals(o1, o2);
            assertEquals(o1.hashCode(), o2.hashCode());
        }

        @Test
        @DisplayName("Deve ser diferente com IDs distintos")
        void deveSerDiferenteComIdsDiferentes() {
            Order o1 = new Order();
            o1.setId(1L);

            Order o2 = new Order();
            o2.setId(2L);

            assertNotEquals(o1, o2);
        }

        @Test
        @DisplayName("Deve testar toString")
        void deveTestarToString() {
            Order order = new Order();
            order.setId(5L);
            order.setClientName("Teste");

            String str = order.toString();
            assertNotNull(str);
            assertTrue(str.contains("Teste"));
        }
    }

    @Nested
    @DisplayName("OrderItem")
    class OrderItemTests {

        @Test
        @DisplayName("Deve calcular subtotal corretamente")
        void deveCalcularSubtotalCorretamente() {
            OrderItem item = new OrderItem();
            item.setQuantity(5);
            item.setUnitPrice(new BigDecimal("19.90"));

            item.calculateSubtotal();

            assertEquals(new BigDecimal("99.50"), item.getSubtotal());
        }

        @Test
        @DisplayName("Não deve calcular subtotal com quantidade null")
        void naoDeveCalcularSubtotalComQuantidadeNull() {
            OrderItem item = new OrderItem();
            item.setQuantity(null);
            item.setUnitPrice(new BigDecimal("10.00"));

            item.calculateSubtotal();

            assertNull(item.getSubtotal());
        }

        @Test
        @DisplayName("Não deve calcular subtotal com preço unitário null")
        void naoDeveCalcularSubtotalComPrecoNull() {
            OrderItem item = new OrderItem();
            item.setQuantity(3);
            item.setUnitPrice(null);

            item.calculateSubtotal();

            assertNull(item.getSubtotal());
        }

        @Test
        @DisplayName("Deve definir e obter todos os campos do OrderItem")
        void deveDefinirEObterTodosOsCampos() {
            Product produto = criarProduto(1L, "Fone", new BigDecimal("89.90"));
            Order order = new Order();
            order.setId(10L);

            OrderItem item = new OrderItem();
            item.setId(77L);
            item.setOrder(order);
            item.setProduct(produto);
            item.setQuantity(2);
            item.setUnitPrice(new BigDecimal("89.90"));
            item.setSubtotal(new BigDecimal("179.80"));

            assertEquals(77L, item.getId());
            assertEquals(order, item.getOrder());
            assertEquals(produto, item.getProduct());
            assertEquals(2, item.getQuantity());
            assertEquals(new BigDecimal("89.90"), item.getUnitPrice());
            assertEquals(new BigDecimal("179.80"), item.getSubtotal());
        }

        @Test
        @DisplayName("Deve criar OrderItem via builder")
        void deveCriarOrderItemViaBuilder() {
            Product produto = criarProduto(1L, "Pendrive", new BigDecimal("35.00"));

            OrderItem item = OrderItem.builder()
                    .id(1L)
                    .product(produto)
                    .quantity(2)
                    .unitPrice(new BigDecimal("35.00"))
                    .subtotal(new BigDecimal("70.00"))
                    .build();

            assertEquals(1L, item.getId());
            assertEquals("Pendrive", item.getProduct().getName());
            assertEquals(new BigDecimal("70.00"), item.getSubtotal());
        }

        @Test
        @DisplayName("Deve testar equals por ID")
        void deveTestarEqualsPorId() {
            OrderItem i1 = new OrderItem();
            i1.setId(1L);

            OrderItem i2 = new OrderItem();
            i2.setId(1L);

            assertEquals(i1, i2);
            assertEquals(i1.hashCode(), i2.hashCode());
        }

        @Test
        @DisplayName("Deve ser diferente com IDs distintos")
        void deveSerDiferenteComIdsDiferentes() {
            OrderItem i1 = new OrderItem();
            i1.setId(1L);

            OrderItem i2 = new OrderItem();
            i2.setId(2L);

            assertNotEquals(i1, i2);
        }
    }

    @Nested
    @DisplayName("OrderStatus")
    class OrderStatusTests {

        @Test
        @DisplayName("Deve conter todos os status esperados")
        void deveConterTodosOsStatusEsperados() {
            OrderStatus[] valores = OrderStatus.values();

            assertEquals(4, valores.length);
            assertNotNull(OrderStatus.valueOf("PENDING"));
            assertNotNull(OrderStatus.valueOf("CONFIRMED"));
            assertNotNull(OrderStatus.valueOf("DELIVERED"));
            assertNotNull(OrderStatus.valueOf("CANCELLED"));
        }

        @Test
        @DisplayName("Deve retornar displayName correto para PENDING")
        void deveRetornarDisplayNamePending() {
            assertEquals("Pendente", OrderStatus.PENDING.getDisplayName());
        }

        @Test
        @DisplayName("Deve retornar displayName correto para CONFIRMED")
        void deveRetornarDisplayNameConfirmed() {
            assertEquals("Confirmado", OrderStatus.CONFIRMED.getDisplayName());
        }

        @Test
        @DisplayName("Deve retornar displayName correto para DELIVERED")
        void deveRetornarDisplayNameDelivered() {
            assertEquals("Entregue", OrderStatus.DELIVERED.getDisplayName());
        }

        @Test
        @DisplayName("Deve retornar displayName correto para CANCELLED")
        void deveRetornarDisplayNameCancelled() {
            assertEquals("Cancelado", OrderStatus.CANCELLED.getDisplayName());
        }
    }

    @Nested
    @DisplayName("Lifecycle callbacks")
    class LifecycleTests {

        @Test
        @DisplayName("onCreate deve preencher timestamps e status PENDING")
        void onCreateDevePreencherTimestamps() {
            Order order = new Order();
            order.setClientName("Teste");
            order.setClientEmail("teste@email.com");
            order.setItems(new ArrayList<>());

            order.onCreate();

            assertNotNull(order.getCreatedAt());
            assertNotNull(order.getUpdatedAt());
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(BigDecimal.ZERO, order.getTotalValue());
        }

        @Test
        @DisplayName("onCreate não deve sobrescrever status se já definido")
        void onCreateNaoSobrescreveStatus() {
            Order order = new Order();
            order.setClientName("Teste");
            order.setClientEmail("teste@email.com");
            order.setStatus(OrderStatus.CONFIRMED);
            order.setItems(new ArrayList<>());

            order.onCreate();

            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        }

        @Test
        @DisplayName("onUpdate deve atualizar updatedAt")
        void onUpdateDeveAtualizarTimestamp() {
            Order order = new Order();
            order.setItems(new ArrayList<>());

            order.onUpdate();

            assertNotNull(order.getUpdatedAt());
        }

        @Test
        @DisplayName("OrderItem.onSaveOrUpdate deve calcular subtotal")
        void orderItemOnSaveDeveCalcularSubtotal() {
            OrderItem item = new OrderItem();
            item.setQuantity(3);
            item.setUnitPrice(new BigDecimal("100.00"));

            item.onSaveOrUpdate();

            assertEquals(new BigDecimal("300.00"), item.getSubtotal());
        }

        @Test
        @DisplayName("Order AllArgsConstructor deve funcionar")
        void orderAllArgsConstructor() {
            Order order = new Order(1L, "Nome", "email@test.com",
                    OrderStatus.PENDING, new BigDecimal("100.00"),
                    new ArrayList<>(), null, null);

            assertEquals(1L, order.getId());
            assertEquals("Nome", order.getClientName());
        }

        @Test
        @DisplayName("OrderItem AllArgsConstructor deve funcionar")
        void orderItemAllArgsConstructor() {
            Product p = criarProduto(1L, "Prod", new BigDecimal("50.00"));
            OrderItem item = new OrderItem(1L, null, p, 2,
                    new BigDecimal("50.00"), new BigDecimal("100.00"));

            assertEquals(1L, item.getId());
            assertEquals(2, item.getQuantity());
        }
    }
}
