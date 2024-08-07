package com.yunusemrenalbant.order.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunusemrenalbant.order.service.OrderOutboxService;
import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jdk.jfr.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.debezium.data.Envelope.FieldName.AFTER;
import static io.debezium.data.Envelope.FieldName.OPERATION;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;

@Slf4j
@Component
public class DebeziumUtil {

    private final Executor executor;
    private final DebeziumEngine<ChangeEvent<String, String>> debeziumEngine;
    private final OrderOutboxService outboxService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    private void start() {
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }
    }

    public DebeziumUtil(Configuration postgresConnector, OrderOutboxService outboxService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.executor = Executors.newSingleThreadExecutor();

        this.debeziumEngine = DebeziumEngine.create(Json.class)
                .using(postgresConnector.asProperties())
                .notifying(this::handleEvent)
                .build();
        this.outboxService = outboxService;
    }

    private void handleEvent(ChangeEvent<String, String> event) {
        log.info("Received event: {}", event);

        try {
            JsonNode eventNode = objectMapper.readTree(event.value());

            JsonNode afterNode = eventNode.path("payload").path("after");

            if (!afterNode.isMissingNode()) {
                Map<String, Object> payload = objectMapper.convertValue(afterNode, new TypeReference<>() {});

                log.info("Extracted 'after' field: {}", payload);

              outboxService.processDebeziumEvent(payload);
            }
        } catch (IOException e) {
            log.error("Error processing event: ", e);
        }
    }

}