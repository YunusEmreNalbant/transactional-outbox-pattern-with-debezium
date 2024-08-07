package com.yunusemrenalbant.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "order-created", groupId = "order-group")
    public void sendNotification(String message) {
        System.out.println("Sending notification: " + message);
    }
}
