package com.crud.system.repository;

import com.crud.system.model.Order;
import com.crud.system.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByClientEmail(String clientEmail);

    List<Order> findByClientNameIgnoreCase(String clientName);
}
