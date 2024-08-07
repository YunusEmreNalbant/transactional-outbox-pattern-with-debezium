package com.yunusemrenalbant.order.repository;

import com.yunusemrenalbant.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
