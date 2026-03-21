package com.crud.system.controller;

import com.crud.system.dto.CreateOrderRequestDTO;
import com.crud.system.dto.OrderItemDTO;
import com.crud.system.dto.OrderMapper;
import com.crud.system.dto.OrderResponseDTO;
import com.crud.system.model.Order;
import com.crud.system.model.OrderItem;
import com.crud.system.model.OrderStatus;
import com.crud.system.service.IOrderService;
import com.crud.system.service.IProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para pedidos.
 * Integra com ProductService pra validar estoque na criação.
 */
@RestController
@RequestMapping("/api/orders")
@Validated
@CrossOrigin(origins = "*")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final IOrderService orderService;
    private final IProductService productService;

    @Autowired
    public OrderController(IOrderService orderService, IProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    /** POST /api/orders - Cria um pedido a partir do DTO. */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO requestDTO) {
        logger.info("POST /api/orders - cliente: {}", requestDTO.getClientName());

        Order order = new Order();
        order.setClientName(requestDTO.getClientName());
        order.setClientEmail(requestDTO.getClientEmail());

        for (OrderItemDTO itemDTO : requestDTO.getItems()) {
            var product = productService.getProductById(itemDTO.getProductId());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(product.getPrice());

            order.addItem(item);
        }

        Order created = orderService.createOrder(order);
        return new ResponseEntity<>(OrderMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    /** GET /api/orders - Lista todos os pedidos. */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        var orders = orderService.getAllOrders();
        var dtos = orders.stream()
            .map(OrderMapper::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** GET /api/orders/{id} - Busca pedido por ID. */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable @NotNull Long id) {
        var order = orderService.getOrderById(id);
        return ResponseEntity.ok(OrderMapper.toResponseDTO(order));
    }

    /** PUT /api/orders/{id}/confirm - Confirma o pedido. */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderResponseDTO> confirmOrder(@PathVariable @NotNull Long id) {
        var confirmed = orderService.confirmOrder(id);
        return ResponseEntity.ok(OrderMapper.toResponseDTO(confirmed));
    }

    /** PUT /api/orders/{id}/deliver - Marca como entregue. */
    @PutMapping("/{id}/deliver")
    public ResponseEntity<OrderResponseDTO> deliverOrder(@PathVariable @NotNull Long id) {
        var delivered = orderService.deliverOrder(id);
        return ResponseEntity.ok(OrderMapper.toResponseDTO(delivered));
    }

    /** PUT /api/orders/{id}/cancel - Cancela e devolve o estoque. */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable @NotNull Long id) {
        var cancelled = orderService.cancelOrder(id);
        return ResponseEntity.ok(OrderMapper.toResponseDTO(cancelled));
    }

    /** GET /api/orders/status/{status} - Filtra por status. */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@PathVariable @NotNull String status) {
        try {
            var orderStatus = OrderStatus.valueOf(status.toUpperCase());
            var orders = orderService.getOrdersByStatus(orderStatus);
            var dtos = orders.stream()
                .map(OrderMapper::toResponseDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }

    /** GET /api/orders/client?email={email} - Pedidos por email do cliente. */
    @GetMapping("/client")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByClientEmail(@RequestParam @NotNull String email) {
        var orders = orderService.getOrdersByClientEmail(email);
        var dtos = orders.stream()
            .map(OrderMapper::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** DELETE /api/orders/{id} - Remove um pedido. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable @NotNull Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
