package ru.itmo.is.musicband.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.musicband.domain.DeadLetterQueue;
import ru.itmo.is.musicband.domain.OutboxEvent;
import ru.itmo.is.musicband.repo.DeadLetterQueueRepository;
import ru.itmo.is.musicband.repo.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_LIMIT = 50;

    private final OutboxEventRepository repo;
    private final DeadLetterQueueRepository dlqRepo;
    private final JmsTemplate jms;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishBatch() {
        List<OutboxEvent> batch = repo.pickBatch(LocalDateTime.now(), PageRequest.of(0, BATCH_LIMIT));
        if (batch.isEmpty()) {
            return;
        }
        log.info("OutboxPublisher: publishing {} events to JMS", batch.size());
        List<OutboxEvent> toDelete = new ArrayList<>();
        for (OutboxEvent e : batch) {
            try {
                String payload = e.getPayload();
                if (payload != null) {
                    jms.convertAndSend("band.events", payload, m -> {
                        m.setLongProperty("eventId", e.getId());
                        return m;
                    });
                }
                e.setStatus("SENT");
                e.setNextRunAt(null);
            } catch (Exception ex) {
                int nextAttempts = e.getAttempts() + 1;
                e.setAttempts(nextAttempts);
                if (nextAttempts > MAX_ATTEMPTS) {
                    moveToDlq(e, ex.getMessage());
                    toDelete.add(e);
                    log.warn("Outbox event {} moved to DLQ after {} attempts", e.getId(), nextAttempts);
                } else {
                    e.setNextRunAt(LocalDateTime.now().plusSeconds(5L * nextAttempts));
                    log.warn("Outbox event {} send failed, will retry (attempt {}/{})", e.getId(), nextAttempts, MAX_ATTEMPTS, ex);
                }
            }
        }
        repo.saveAll(batch);
        if (!toDelete.isEmpty()) {
            repo.deleteAll(toDelete);
        }
    }

    private void moveToDlq(OutboxEvent e, String error) {
        DeadLetterQueue dlq = new DeadLetterQueue();
        dlq.setOriginalEventId(e.getId());
        dlq.setAggregateType(e.getAggregateType());
        dlq.setAggregateId(e.getAggregateId());
        dlq.setEventType(e.getEventType());
        dlq.setPayload(e.getPayload());
        dlq.setAttempts(e.getAttempts());
        dlq.setLastError(error);
        dlqRepo.save(dlq);
    }
}

