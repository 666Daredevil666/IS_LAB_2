package ru.itmo.is.musicband.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.musicband.domain.ImportOperation;
import ru.itmo.is.musicband.domain.MusicBand;
import ru.itmo.is.musicband.domain.OutboxEvent;
import ru.itmo.is.musicband.dto.BandEvent;
import ru.itmo.is.musicband.repo.OutboxEventRepository;

@Component
@RequiredArgsConstructor
public class OutboxHelper {
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher events;

    @Transactional
    public void writeOutbox(MusicBand band, String eventType) {
        OutboxEvent e = new OutboxEvent();
        e.setAggregateType("MusicBand");
        e.setAggregateId(band.getId());
        e.setEventType(eventType);
        e.setPayload(toJson(new BandEvent(eventType, band.getId())));
        e.setStatus("NEW");
        outboxRepo.save(e);
        events.publishEvent(new BandChangedEvent(eventType, band.getId()));
    }

    @Transactional
    public void publishImport(ImportOperation op) {
        if (op != null) {
            events.publishEvent(op);
        }
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payload", e);
        }
    }
}

