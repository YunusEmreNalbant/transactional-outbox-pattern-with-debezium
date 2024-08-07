package com.yunusemrenalbant.order.controller;

import com.yunusemrenalbant.order.model.Order;
import com.yunusemrenalbant.order.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public UUID createOrder(@RequestBody Order newOrder) {
        return orderService.createOrder(newOrder);
    }
}
