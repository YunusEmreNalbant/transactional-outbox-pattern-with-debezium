package com.yunusemrenalbant.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunusemrenalbant.order.enums.OrderStatus;
import com.yunusemrenalbant.order.model.Outbox;
import com.yunusemrenalbant.order.publisher.KafkaPublisher;
import com.yunusemrenalbant.order.repository.OrderOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderOutboxService {

    private final OrderOutboxRepository orderOutboxRepository;
    private final KafkaPublisher kafkaPublisher;
    private final ObjectMapper MAPPER = new ObjectMapper();

    public OrderOutboxService(
            OrderOutboxRepository orderOutboxRepository,
            KafkaPublisher kafkaPublisher
    ) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.kafkaPublisher = kafkaPublisher;
    }

    /**
     * Processes a Debezium event payload.
     *
     * @param eventPayload The payload of the Debezium event.
     *                     The payload should be a Map containing the event type and other relevant data.
     *                     The event type should be a String representing the status of the order.
     *                     The payload should also contain other relevant data for the order.
     * @throws RuntimeException if there is an error processing the JSON payload.
     */
    public void processDebeziumEvent(Map<String, Object> eventPayload) {
        log.info("Received Debezium event payload: {}", eventPayload);

        if (OrderStatus.CREATED.name().equals(eventPayload.get("event_type"))) {
            try {
                String payloadAsString = MAPPER.writeValueAsString(eventPayload);
                kafkaPublisher.publish("order-created", payloadAsString);

                log.info("Serialized Debezium event payload: {}", payloadAsString);
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON payload", e);
                throw new RuntimeException("Failed to process event payload", e);
            }
        }
    }

    /**
     * Processes outbox events at a fixed rate of 5000 milliseconds (5 seconds).
     * Retrieves a list of outbox event entities based on the event type 'CREATED'.
     * If the list is not empty, iterates over each outbox event entity and performs the following steps:
     * - Logs the information about sending the event to Kafka.
     * - Publishes the outbox event entity to Kafka using the KafkaPublisher.
     * - Deletes the outbox event entity from the repository.
     */
    @Scheduled(fixedRate = 5000)
    public void processOutboxEvents() {
        log.info("Processing outbox events...");

        List<Outbox> listOfOutboxEventEntities = orderOutboxRepository.findByEventType(OrderStatus.CREATED);

        log.info("Number of outbox events: {}", listOfOutboxEventEntities.size());

        if (!listOfOutboxEventEntities.isEmpty()) {
            for (Outbox outboxEventEntity : listOfOutboxEventEntities) {
                log.info("Sending event to Kafka");

                kafkaPublisher.publish("order-created", outboxEventEntity);

                orderOutboxRepository.deleteById(outboxEventEntity.getId());
            }
        }
    }

}
