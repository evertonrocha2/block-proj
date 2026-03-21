package com.crud.system.service;

import com.crud.system.model.Order;
import com.crud.system.model.OrderStatus;
import java.util.List;

public interface IOrderService {

    Order createOrder(Order order);

    List<Order> getAllOrders();

    Order getOrderById(Long id);

    Order updateOrderStatus(Long id, OrderStatus status);

    void deleteOrder(Long id);

    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByClientEmail(String clientEmail);

    Order confirmOrder(Long id);

    Order deliverOrder(Long id);

    Order cancelOrder(Long id);
}
