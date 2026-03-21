package com.crud.system.controller;

import com.crud.system.dto.CreateOrderRequestDTO;
import com.crud.system.dto.OrderItemDTO;
import com.crud.system.model.Order;
import com.crud.system.model.OrderItem;
import com.crud.system.model.OrderStatus;
import com.crud.system.model.Product;
import com.crud.system.service.OrderService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController - Testes")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductService productService;

    private Product sampleProduct;
    private Order sampleOrder;
    private CreateOrderRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Notebook Dell")
                .description("Notebook Dell Inspiron 15, 8GB RAM, 256GB SSD")
                .price(new BigDecimal("2500.00"))
                .quantity(10)
                .category("Eletrônicos")
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .product(sampleProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("2500.00"))
                .subtotal(new BigDecimal("5000.00"))
                .build();

        sampleOrder = Order.builder()
                .id(1L)
                .clientName("João Silva")
                .clientEmail("joao@email.com")
                .status(OrderStatus.PENDING)
                .totalValue(new BigDecimal("5000.00"))
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        item.setOrder(sampleOrder);

        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);

        validRequest = new CreateOrderRequestDTO();
        validRequest.setClientName("João Silva");
        validRequest.setClientEmail("joao@email.com");
        validRequest.setItems(List.of(itemDTO));
    }

    @Test
    @DisplayName("POST /api/orders - Deve criar pedido com sucesso")
    void deveCriarPedidoComSucesso() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(sampleProduct);
        when(orderService.createOrder(ArgumentMatchers.any(Order.class))).thenReturn(sampleOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientName").value("João Silva"))
                .andExpect(jsonPath("$.clientEmail").value("joao@email.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalValue").value(5000.00))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(productService).getProductById(1L);
        verify(orderService).createOrder(ArgumentMatchers.any(Order.class));
    }

    @Test
    @DisplayName("POST /api/orders - Deve falhar com lista de itens vazia")
    void deveFalharComListaDeItensVazia() throws Exception {
        // Arrange
        CreateOrderRequestDTO requestSemItens = new CreateOrderRequestDTO();
        requestSemItens.setClientName("João Silva");
        requestSemItens.setClientEmail("joao@email.com");
        requestSemItens.setItems(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestSemItens)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(ArgumentMatchers.any(Order.class));
    }

    @Test
    @DisplayName("POST /api/orders - Deve falhar com campos obrigatórios ausentes")
    void deveFalharComCamposObrigatoriosAusentes() throws Exception {
        // Arrange
        CreateOrderRequestDTO requestInvalido = new CreateOrderRequestDTO();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(ArgumentMatchers.any(Order.class));
    }

    @Test
    @DisplayName("GET /api/orders - Deve retornar todos os pedidos")
    void deveRetornarTodosOsPedidos() throws Exception {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(sampleOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientName").value("João Silva"));

        verify(orderService).getAllOrders();
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Deve retornar pedido por ID")
    void deveRetornarPedidoPorId() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(sampleOrder);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientName").value("João Silva"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).getOrderById(1L);
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/confirm - Deve confirmar pedido")
    void deveConfirmarPedido() throws Exception {
        // Arrange
        Order confirmado = Order.builder()
                .id(1L)
                .clientName("João Silva")
                .clientEmail("joao@email.com")
                .status(OrderStatus.CONFIRMED)
                .totalValue(new BigDecimal("5000.00"))
                .items(sampleOrder.getItems())
                .createdAt(sampleOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.confirmOrder(1L)).thenReturn(confirmado);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).confirmOrder(1L);
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/deliver - Deve marcar pedido como entregue")
    void deveMarcarPedidoComoEntregue() throws Exception {
        // Arrange
        Order entregue = Order.builder()
                .id(1L)
                .clientName("João Silva")
                .clientEmail("joao@email.com")
                .status(OrderStatus.DELIVERED)
                .totalValue(new BigDecimal("5000.00"))
                .items(sampleOrder.getItems())
                .createdAt(sampleOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.deliverOrder(1L)).thenReturn(entregue);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(orderService).deliverOrder(1L);
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/cancel - Deve cancelar pedido")
    void deveCancelarPedido() throws Exception {
        // Arrange
        Order cancelado = Order.builder()
                .id(1L)
                .clientName("João Silva")
                .clientEmail("joao@email.com")
                .status(OrderStatus.CANCELLED)
                .totalValue(new BigDecimal("5000.00"))
                .items(sampleOrder.getItems())
                .createdAt(sampleOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.cancelOrder(1L)).thenReturn(cancelado);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService).cancelOrder(1L);
    }

    @Test
    @DisplayName("GET /api/orders/status/{status} - Deve filtrar pedidos por status")
    void deveFiltrarPedidosPorStatus() throws Exception {
        // Arrange
        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(Arrays.asList(sampleOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(orderService).getOrdersByStatus(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("GET /api/orders/status/{status} - Deve falhar com status inválido")
    void deveFalharComStatusInvalido() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/status/INVALIDO"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrdersByStatus(ArgumentMatchers.any(OrderStatus.class));
    }

    @Test
    @DisplayName("GET /api/orders/client?email= - Deve buscar pedidos por email do cliente")
    void deveBuscarPedidosPorEmailDoCliente() throws Exception {
        // Arrange
        when(orderService.getOrdersByClientEmail("joao@email.com")).thenReturn(Arrays.asList(sampleOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/client")
                .param("email", "joao@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientEmail").value("joao@email.com"));

        verify(orderService).getOrdersByClientEmail("joao@email.com");
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Deve deletar pedido")
    void deveDeletarPedido() throws Exception {
        // Arrange
        doNothing().when(orderService).deleteOrder(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L);
    }
}
