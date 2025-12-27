package ru.itmo.is.musicband.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import ru.itmo.is.musicband.domain.DeadLetterQueue;
import ru.itmo.is.musicband.domain.DeliveryLog;
import ru.itmo.is.musicband.repo.DeadLetterQueueRepository;
import ru.itmo.is.musicband.repo.DeliveryLogRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class JmsEventListener {
    private static final int MAX_DELIVERY_ATTEMPTS = 3;

    private final DeliveryLogRepository logRepo;
    private final DeadLetterQueueRepository dlqRepo;
    private final WebClient webClient;
    private final TransactionTemplate transactionTemplate;

    public JmsEventListener(DeliveryLogRepository logRepo,
                            DeadLetterQueueRepository dlqRepo,
                            PlatformTransactionManager transactionManager,
                            @Value("${external.rest.url:http://external-service/api/events}") String restUrl) {
        this.logRepo = logRepo;
        this.dlqRepo = dlqRepo;
        this.webClient = WebClient.builder().baseUrl(restUrl != null ? restUrl : "http://external-service/api/events").build();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate = new TransactionTemplate(transactionManager, def);
    }

    @JmsListener(destination = "band.events")
    public void handle(String payload, @Headers Map<String, Object> headers) {
        long eventId = 0L;
        Object headerVal = headers.get("eventId");
        if (headerVal instanceof Number num) {
            eventId = num.longValue();
        }

        log.warn("JmsEventListener received event {} from JMS queue", eventId);

        DeliveryLog existingLog = getDeliveryLog(eventId);
        log.warn("getDeliveryLog for event {} returned: {}", eventId, existingLog != null ? "NOT NULL, status=" + existingLog.getStatus() : "NULL");
        if (existingLog != null && "DELIVERED".equals(existingLog.getStatus())) {
            log.warn("Event {} already delivered, skipping", eventId);
            return;
        }

        DeliveryLog deliveryLog = existingLog;
        String idempotencyToken;

        if (deliveryLog == null) {
            log.warn("deliveryLog is NULL, calling saveDeliveryLog for event {}", eventId);
            saveDeliveryLog(eventId);
            deliveryLog = getDeliveryLog(eventId);
            log.warn("After saveDeliveryLog, getDeliveryLog returned: {}", deliveryLog != null ? "NOT NULL" : "NULL");
            if (deliveryLog == null) {
                log.error("Failed to create delivery log for event {}", eventId);
                return;
            }
            idempotencyToken = deliveryLog.getIdempotencyToken();
            log.warn("Created delivery log for event {} with status RECEIVED", eventId);
        } else {
            idempotencyToken = deliveryLog.getIdempotencyToken();
            log.warn("Using existing delivery log for event {}, status: {}", eventId, deliveryLog.getStatus());
        }

        try {
            String confirmationToken = webClient.post()
                    .header("Idempotency-Key", idempotencyToken != null ? idempotencyToken : "")
                    .bodyValue(payload != null ? payload : "")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            updateDeliveryLogStatus(eventId, "DELIVERED", deliveryLog.getAttempts() + 1, null);
            log.info("Successfully delivered event {} to external service (confirmation: {})", eventId, confirmationToken);
        } catch (Exception ex) {
            int attempts = (deliveryLog != null ? deliveryLog.getAttempts() : 0) + 1;
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();

            if (attempts >= MAX_DELIVERY_ATTEMPTS) {
                updateDeliveryLogStatus(eventId, "DLQ", attempts, errorMsg);
                moveToDlq(eventId, payload, attempts, errorMsg);
                log.warn("Event {} moved to DLQ after {} delivery attempts", eventId, attempts);
            } else {
                updateDeliveryLogStatus(eventId, "RETRY", attempts, errorMsg);
                log.warn("Event {} delivery failed, will retry (attempt {}/{})", eventId, attempts, MAX_DELIVERY_ATTEMPTS);
                throw new RuntimeException("Delivery failed, triggering JMS redelivery", ex);
            }
        }
    }

    private DeliveryLog getDeliveryLog(long eventId) {
        return transactionTemplate.execute(txStatus -> {
            return logRepo.findByEventId(eventId).orElse(null);
        });
    }

    private void saveDeliveryLog(long eventId) {
        log.warn("saveDeliveryLog START for event {}", eventId);
        try {
            transactionTemplate.executeWithoutResult(txStatus -> {
                String idempotencyToken = java.util.UUID.randomUUID().toString();
                DeliveryLog deliveryLog = new DeliveryLog();
                deliveryLog.setEventId(eventId);
                deliveryLog.setIdempotencyToken(idempotencyToken);
                deliveryLog.setStatus("RECEIVED");
                deliveryLog.setAttempts(0);
                DeliveryLog saved = logRepo.saveAndFlush(deliveryLog);
                log.warn("Saved delivery log for event {} with id {} in transaction", eventId, saved.getId());
            });
            log.warn("saveDeliveryLog END for event {}", eventId);
        } catch (Exception e) {
            log.error("saveDeliveryLog ERROR for event {}: {}", eventId, e.getMessage(), e);
            throw e;
        }
    }

    private void updateDeliveryLogStatus(long eventId, String status, int attempts, String error) {
        transactionTemplate.executeWithoutResult(txStatus -> {
            DeliveryLog deliveryLog = logRepo.findByEventId(eventId).orElse(null);
            if (deliveryLog != null) {
                deliveryLog.setStatus(status);
                deliveryLog.setAttempts(attempts);
                deliveryLog.setLastError(error);
                if ("DELIVERED".equals(status)) {
                    deliveryLog.setDeliveredAt(LocalDateTime.now());
                }
                logRepo.saveAndFlush(deliveryLog);
            } else {
                log.warn("DeliveryLog not found for event {} when trying to update status to {}", eventId, status);
            }
        });
    }

    private void moveToDlq(long eventId, String payload, int attempts, String error) {
        transactionTemplate.executeWithoutResult(txStatus -> {
            DeadLetterQueue dlq = new DeadLetterQueue();
            dlq.setOriginalEventId(eventId);
            dlq.setAggregateType("JmsDelivery");
            dlq.setAggregateId(eventId);
            dlq.setEventType("EXTERNAL_DELIVERY_FAILED");
            dlq.setPayload(payload);
            dlq.setAttempts(attempts);
            dlq.setLastError(error);
            dlqRepo.saveAndFlush(dlq);
        });
    }

}

