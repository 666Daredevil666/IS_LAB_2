package ru.itmo.is.musicband.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    public JmsEventListener(DeliveryLogRepository logRepo,
                            DeadLetterQueueRepository dlqRepo,
                            @Value("${external.rest.url:http://external-service/api/events}") String restUrl) {
        this.logRepo = logRepo;
        this.dlqRepo = dlqRepo;
        this.webClient = WebClient.builder().baseUrl(restUrl != null ? restUrl : "http://external-service/api/events").build();
    }

    @JmsListener(destination = "band.events")
    @Transactional(noRollbackFor = RuntimeException.class)
    public void handle(String payload, @Headers Map<String, Object> headers) {
        long eventId = 0L;
        Object headerVal = headers.get("eventId");
        if (headerVal instanceof Number num) {
            eventId = num.longValue();
        }

        DeliveryLog existingLog = logRepo.findByEventId(eventId).orElse(null);
        if (existingLog != null && "DELIVERED".equals(existingLog.getStatus())) {
            log.debug("Event {} already delivered, skipping", eventId);
            return;
        }

        DeliveryLog deliveryLog = existingLog;
        String idempotencyToken;

        if (deliveryLog == null) {
            idempotencyToken = java.util.UUID.randomUUID().toString();
            deliveryLog = new DeliveryLog();
            deliveryLog.setEventId(eventId);
            deliveryLog.setIdempotencyToken(idempotencyToken);
            deliveryLog.setStatus("RECEIVED");
            logRepo.save(deliveryLog);
        } else {
            idempotencyToken = deliveryLog.getIdempotencyToken();
        }

        try {
            String confirmationToken = webClient.post()
                    .header("Idempotency-Key", idempotencyToken != null ? idempotencyToken : "")
                    .bodyValue(payload != null ? payload : "")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            deliveryLog.setStatus("DELIVERED");
            deliveryLog.setAttempts(deliveryLog.getAttempts() + 1);
            deliveryLog.setDeliveredAt(LocalDateTime.now());
            deliveryLog.setLastError(null);
            logRepo.save(deliveryLog);
            log.info("Successfully delivered event {} to external service (confirmation: {})", eventId, confirmationToken);
        } catch (Exception ex) {
            int attempts = deliveryLog.getAttempts() + 1;
            deliveryLog.setAttempts(attempts);
            deliveryLog.setLastError(ex.getMessage());

            if (attempts >= MAX_DELIVERY_ATTEMPTS) {
                deliveryLog.setStatus("DLQ");
                logRepo.save(deliveryLog);
                moveToDlq(eventId, payload, attempts, ex.getMessage());
                log.warn("Event {} moved to DLQ after {} delivery attempts", eventId, attempts);
            } else {
                deliveryLog.setStatus("RETRY");
                logRepo.save(deliveryLog);
                log.warn("Event {} delivery failed, will retry (attempt {}/{})", eventId, attempts, MAX_DELIVERY_ATTEMPTS);
                throw new RuntimeException("Delivery failed, triggering JMS redelivery", ex);
            }
        }
    }

    private void moveToDlq(long eventId, String payload, int attempts, String error) {
        DeadLetterQueue dlq = new DeadLetterQueue();
        dlq.setOriginalEventId(eventId);
        dlq.setAggregateType("JmsDelivery");
        dlq.setAggregateId(eventId);
        dlq.setEventType("EXTERNAL_DELIVERY_FAILED");
        dlq.setPayload(payload);
        dlq.setAttempts(attempts);
        dlq.setLastError(error);
        dlqRepo.save(dlq);
    }

}

