package com.crud.system.service;

import com.crud.system.exception.BusinessRuleException;
import com.crud.system.exception.ResourceNotFoundException;
import com.crud.system.model.Order;
import com.crud.system.model.OrderItem;
import com.crud.system.model.OrderStatus;
import com.crud.system.model.Product;
import com.crud.system.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para OrderService.
 * Cobre todos os métodos e branches, incluindo cenários de falha.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service - Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private IProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Product validProduct;
    private Product validProduct2;
    private Order validOrder;
    private OrderItem validItem;

    @BeforeEach
    void setUp() {
        validProduct = Product.builder()
                .id(1L)
                .name("Notebook Dell")
                .description("Notebook Dell Inspiron 15")
                .price(new BigDecimal("2500.00"))
                .quantity(10)
                .category("Eletrônicos")
                .build();

        validProduct2 = Product.builder()
                .id(2L)
                .name("Mouse Logitech")
                .description("Mouse sem fio")
                .price(new BigDecimal("150.00"))
                .quantity(20)
                .category("Periféricos")
                .build();

        validItem = OrderItem.builder()
                .id(1L)
                .product(validProduct)
                .quantity(2)
                .unitPrice(validProduct.getPrice())
                .subtotal(new BigDecimal("5000.00"))
                .build();

        List<OrderItem> items = new ArrayList<>();
        items.add(validItem);

        validOrder = Order.builder()
                .id(1L)
                .clientName("João Silva")
                .clientEmail("joao@email.com")
                .status(OrderStatus.PENDING)
                .totalValue(new BigDecimal("5000.00"))
                .items(items)
                .build();
    }

    // ============================
    //  CREATE ORDER
    // ============================

    @Test
    @DisplayName("Deve criar pedido com sucesso")
    void shouldCreateOrderSuccessfully() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(validProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // Act
        Order result = orderService.createOrder(validOrder);

        // Assert
        assertNotNull(result);
        assertEquals(validOrder.getId(), result.getId());
        assertEquals("João Silva", result.getClientName());
        verify(productService).getProductById(1L);
        verify(productService).updateProduct(eq(1L), any(Product.class));
        verify(orderRepository).save(validOrder);
    }

    @Test
    @DisplayName("Deve falhar ao criar pedido sem itens")
    void shouldFailToCreateOrderWithNoItems() {
        // Arrange
        Order emptyOrder = Order.builder()
                .clientName("João Silva")
                .clientEmail("joao@email.com")
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.createOrder(emptyOrder)
        );

        assertTrue(exception.getMessage().contains("pelo menos um item"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao criar pedido com itens nulos")
    void shouldFailToCreateOrderWithNullItems() {
        // Arrange
        Order nullItemsOrder = Order.builder()
                .clientName("João Silva")
                .clientEmail("joao@email.com")
                .status(OrderStatus.PENDING)
                .items(null)
                .build();

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.createOrder(nullItemsOrder)
        );

        assertTrue(exception.getMessage().contains("pelo menos um item"));
    }

    @Test
    @DisplayName("Deve falhar ao criar pedido com estoque insuficiente")
    void shouldFailToCreateOrderWithInsufficientStock() {
        // Arrange - produto com apenas 1 unidade, pedido quer 2
        Product lowStockProduct = Product.builder()
                .id(1L)
                .name("Notebook Dell")
                .price(new BigDecimal("2500.00"))
                .quantity(1)
                .build();

        when(productService.getProductById(1L)).thenReturn(lowStockProduct);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.createOrder(validOrder)
        );

        assertTrue(exception.getMessage().contains("Estoque insuficiente"));
        assertTrue(exception.getMessage().contains("Notebook Dell"));
        verify(orderRepository, never()).save(any());
        verify(productService, never()).updateProduct(anyLong(), any());
    }

    @Test
    @DisplayName("Deve falhar ao criar pedido com erro no banco de dados")
    void shouldFailToCreateOrderWithDatabaseError() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(validProduct);
        when(orderRepository.save(any())).thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.createOrder(validOrder)
        );

        assertTrue(exception.getMessage().contains("Erro ao criar pedido"));
    }

    // ============================
    //  GET ALL ORDERS
    // ============================

    @Test
    @DisplayName("Deve retornar todos os pedidos")
    void shouldReturnAllOrders() {
        // Arrange
        Order secondOrder = Order.builder()
                .id(2L)
                .clientName("Maria Souza")
                .clientEmail("maria@email.com")
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findAll()).thenReturn(Arrays.asList(validOrder, secondOrder));

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    void shouldReturnEmptyListWhenNoOrders() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve falhar ao buscar todos os pedidos com erro no banco")
    void shouldFailToGetAllOrdersWithDatabaseError() {
        // Arrange
        when(orderRepository.findAll()).thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.getAllOrders()
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar pedidos"));
    }

    // ============================
    //  GET ORDER BY ID
    // ============================

    @Test
    @DisplayName("Deve buscar pedido por ID com sucesso")
    void shouldGetOrderByIdSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));

        // Act
        Order result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("João Silva", result.getClientName());
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar pedido inexistente")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            orderService.getOrderById(999L)
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    @DisplayName("Deve falhar ao buscar pedido com ID inválido")
    void shouldFailToGetOrderWithInvalidId(Long invalidId) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            orderService.getOrderById(invalidId)
        );

        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Deve falhar ao buscar pedido com erro no banco")
    void shouldFailToGetOrderByIdWithDatabaseError() {
        // Arrange
        when(orderRepository.findById(1L)).thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.getOrderById(1L)
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar pedido"));
    }

    // ============================
    //  UPDATE ORDER STATUS
    // ============================

    @Test
    @DisplayName("Deve atualizar status do pedido com sucesso")
    void shouldUpdateOrderStatusSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // Act
        Order result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -50})
    @DisplayName("Deve falhar ao atualizar status com ID inválido")
    void shouldFailToUpdateStatusWithInvalidId(Long invalidId) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            orderService.updateOrderStatus(invalidId, OrderStatus.CONFIRMED)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao atualizar status com erro no banco")
    void shouldFailToUpdateStatusWithDatabaseError() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any())).thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED)
        );

        assertTrue(exception.getMessage().contains("Erro ao atualizar pedido"));
    }

    // ============================
    //  DELETE ORDER
    // ============================

    @Test
    @DisplayName("Deve deletar pedido confirmado e restaurar estoque")
    void shouldDeleteConfirmedOrderAndRestoreStock() {
        // Arrange
        validOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        doNothing().when(orderRepository).delete(validOrder);

        // Act
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));

        // Assert - deve restaurar estoque porque era CONFIRMED
        verify(productService).updateProduct(eq(1L), any(Product.class));
        verify(orderRepository).delete(validOrder);
    }

    @Test
    @DisplayName("Deve deletar pedido pendente sem restaurar estoque")
    void shouldDeletePendingOrderWithoutRestoringStock() {
        // Arrange
        validOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        doNothing().when(orderRepository).delete(validOrder);

        // Act
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));

        // Assert - NÃO deve restaurar estoque porque era PENDING
        verify(productService, never()).updateProduct(anyLong(), any());
        verify(orderRepository).delete(validOrder);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -50})
    @DisplayName("Deve falhar ao deletar pedido com ID inválido")
    void shouldFailToDeleteOrderWithInvalidId(Long invalidId) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            orderService.deleteOrder(invalidId)
        );

        verify(orderRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve falhar ao deletar pedido com erro no banco")
    void shouldFailToDeleteOrderWithDatabaseError() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        doThrow(new DataAccessException("DB Error") {}).when(orderRepository).delete(any());

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.deleteOrder(1L)
        );

        assertTrue(exception.getMessage().contains("Erro ao deletar pedido"));
    }

    // ============================
    //  GET ORDERS BY STATUS
    // ============================

    @Test
    @DisplayName("Deve buscar pedidos por status com sucesso")
    void shouldGetOrdersByStatusSuccessfully() {
        // Arrange
        when(orderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(Arrays.asList(validOrder));

        // Act
        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Deve falhar ao buscar pedidos com status nulo")
    void shouldFailToGetOrdersWithNullStatus() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            orderService.getOrdersByStatus(null)
        );

        verify(orderRepository, never()).findByStatus(any());
    }

    @Test
    @DisplayName("Deve falhar ao buscar pedidos por status com erro no banco")
    void shouldFailToGetOrdersByStatusWithDatabaseError() {
        // Arrange
        when(orderRepository.findByStatus(any()))
                .thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.getOrdersByStatus(OrderStatus.PENDING)
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar pedidos por status"));
    }

    // ============================
    //  GET ORDERS BY CLIENT EMAIL
    // ============================

    @Test
    @DisplayName("Deve buscar pedidos por email do cliente com sucesso")
    void shouldGetOrdersByClientEmailSuccessfully() {
        // Arrange
        when(orderRepository.findByClientEmail("joao@email.com"))
                .thenReturn(Arrays.asList(validOrder));

        // Act
        List<Order> result = orderService.getOrdersByClientEmail("joao@email.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByClientEmail("joao@email.com");
    }

    @Test
    @DisplayName("Deve fazer trim do email antes de buscar")
    void shouldTrimEmailBeforeSearching() {
        // Arrange
        when(orderRepository.findByClientEmail("joao@email.com"))
                .thenReturn(Arrays.asList(validOrder));

        // Act
        List<Order> result = orderService.getOrdersByClientEmail("  joao@email.com  ");

        // Assert
        assertNotNull(result);
        verify(orderRepository).findByClientEmail("joao@email.com");
    }

    @Test
    @DisplayName("Deve falhar ao buscar pedidos com email nulo")
    void shouldFailToGetOrdersWithNullEmail() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            orderService.getOrdersByClientEmail(null)
        );

        verify(orderRepository, never()).findByClientEmail(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deve falhar ao buscar pedidos com email vazio")
    void shouldFailToGetOrdersWithEmptyEmail(String email) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.getOrdersByClientEmail(email)
        );

        assertTrue(exception.getMessage().contains("Email do cliente não pode ser vazio"));
    }

    @Test
    @DisplayName("Deve falhar ao buscar pedidos por email com erro no banco")
    void shouldFailToGetOrdersByEmailWithDatabaseError() {
        // Arrange
        when(orderRepository.findByClientEmail(anyString()))
                .thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.getOrdersByClientEmail("joao@email.com")
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar pedidos por email"));
    }

    // ============================
    //  CONFIRM ORDER
    // ============================

    @Test
    @DisplayName("Deve confirmar pedido com sucesso")
    void shouldConfirmOrderSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // Act
        Order result = orderService.confirmOrder(1L);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    // ============================
    //  DELIVER ORDER
    // ============================

    @Test
    @DisplayName("Deve marcar pedido como entregue com sucesso")
    void shouldDeliverOrderSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // Act
        Order result = orderService.deliverOrder(1L);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    // ============================
    //  CANCEL ORDER
    // ============================

    @Test
    @DisplayName("Deve cancelar pedido com sucesso e restaurar estoque")
    void shouldCancelOrderSuccessfullyAndRestoreStock() {
        // Arrange
        validOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        int stockBefore = validProduct.getQuantity();

        // Act
        Order result = orderService.cancelOrder(1L);

        // Assert
        assertNotNull(result);
        verify(productService).updateProduct(eq(1L), any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve falhar ao cancelar pedido já cancelado")
    void shouldFailToCancelAlreadyCancelledOrder() {
        // Arrange
        validOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.cancelOrder(1L)
        );

        assertTrue(exception.getMessage().contains("já foi cancelado"));
        verify(orderRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    @DisplayName("Deve falhar ao cancelar pedido com ID inválido")
    void shouldFailToCancelOrderWithInvalidId(Long invalidId) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            orderService.cancelOrder(invalidId)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao cancelar pedido com erro no banco")
    void shouldFailToCancelOrderWithDatabaseError() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any())).thenThrow(new DataAccessException("DB Error") {});

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> orderService.cancelOrder(1L)
        );

        assertTrue(exception.getMessage().contains("Erro ao cancelar pedido"));
    }

    // ============================
    //  RESTORE STOCK (via cancelOrder/deleteOrder)
    // ============================

    @Test
    @DisplayName("Deve restaurar estoque de múltiplos itens ao cancelar")
    void shouldRestoreStockForMultipleItemsOnCancel() {
        // Arrange
        OrderItem item2 = OrderItem.builder()
                .id(2L)
                .product(validProduct2)
                .quantity(3)
                .unitPrice(validProduct2.getPrice())
                .subtotal(new BigDecimal("450.00"))
                .build();

        validOrder.getItems().add(item2);
        validOrder.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // Act
        orderService.cancelOrder(1L);

        // Assert - deve restaurar estoque dos dois produtos
        verify(productService).updateProduct(eq(1L), any(Product.class));
        verify(productService).updateProduct(eq(2L), any(Product.class));
    }

    @Test
    @DisplayName("Deve lidar com pedido sem itens ao restaurar estoque (cancelar)")
    void shouldHandleEmptyItemsOnCancelRestore() {
        // Arrange
        validOrder.setItems(new ArrayList<>());
        validOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(validOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // Act - não deve lançar exceção
        assertDoesNotThrow(() -> orderService.cancelOrder(1L));

        // Assert - não deve chamar updateProduct pois não há itens
        verify(productService, never()).updateProduct(anyLong(), any());
    }

    // ============================
    //  CREATE ORDER (estoque com múltiplos itens)
    // ============================

    @Test
    @DisplayName("Deve criar pedido com múltiplos itens e reduzir estoque de todos")
    void shouldCreateOrderWithMultipleItemsAndReduceStock() {
        // Arrange
        OrderItem item2 = OrderItem.builder()
                .id(2L)
                .product(validProduct2)
                .quantity(5)
                .unitPrice(validProduct2.getPrice())
                .subtotal(new BigDecimal("750.00"))
                .build();

        validOrder.getItems().add(item2);

        when(productService.getProductById(1L)).thenReturn(validProduct);
        when(productService.getProductById(2L)).thenReturn(validProduct2);
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // Act
        Order result = orderService.createOrder(validOrder);

        // Assert
        assertNotNull(result);
        verify(productService).updateProduct(eq(1L), any(Product.class));
        verify(productService).updateProduct(eq(2L), any(Product.class));
    }
}
