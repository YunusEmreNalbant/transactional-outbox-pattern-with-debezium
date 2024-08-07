package com.yunusemrenalbant.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunusemrenalbant.order.enums.OrderStatus;
import com.yunusemrenalbant.order.model.Order;
import com.yunusemrenalbant.order.model.Outbox;
import com.yunusemrenalbant.order.repository.OrderOutboxRepository;
import com.yunusemrenalbant.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderRepository orderRepository,
            OrderOutboxRepository orderOutboxRepository,
            ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.orderOutboxRepository = orderOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UUID createOrder(Order order) {
        Order newOrder = orderRepository.save(order);
        log.info("newOrder created...");
        saveOrderOutboxToDatabase(newOrder, OrderStatus.CREATED);

        return newOrder.getId();
    }

    private void saveOrderOutboxToDatabase(Order newOrder, OrderStatus eventType) {
        Outbox orderOutbox = new Outbox();

        try {
            orderOutbox.setPayload(objectMapper.writeValueAsString(newOrder));
            orderOutbox.setEventType(eventType);

            orderOutboxRepository.save(orderOutbox);
            log.info("Order Outbox created: {}", newOrder);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
