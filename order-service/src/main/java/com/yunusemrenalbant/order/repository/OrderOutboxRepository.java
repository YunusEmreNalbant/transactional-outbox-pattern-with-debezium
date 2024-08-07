package com.yunusemrenalbant.order.repository;

import com.yunusemrenalbant.order.enums.OrderStatus;
import com.yunusemrenalbant.order.model.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderOutboxRepository extends JpaRepository<Outbox, UUID> {
    @Query("SELECT o FROM Outbox o WHERE o.eventType = :eventType")
    List<Outbox> findByEventType(@Param("eventType") OrderStatus eventType);
}
