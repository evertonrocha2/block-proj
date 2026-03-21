package com.crud.system.dto;

import com.crud.system.model.Order;
import com.crud.system.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) return null;

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setClientName(order.getClientName());
        dto.setClientEmail(order.getClientEmail());
        dto.setStatus(order.getStatus());
        dto.setTotalValue(order.getTotalValue());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(OrderMapper::itemToDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }

    private static OrderItemDTO itemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
