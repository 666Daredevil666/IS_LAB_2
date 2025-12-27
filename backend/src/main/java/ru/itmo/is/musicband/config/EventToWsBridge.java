package ru.itmo.is.musicband.config;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.dto.BandEvent;
import ru.itmo.is.musicband.service.BandChangedEvent;
import ru.itmo.is.musicband.domain.ImportOperation;

@Component
@RequiredArgsConstructor
public class EventToWsBridge {
    private final SimpMessagingTemplate ws;

    @TransactionalEventListener
    public void onBandChanged(BandChangedEvent e) {
        ws.convertAndSend("/topic/bands", new BandEvent(e.type(), e.id()));
    }

    @TransactionalEventListener
    public void onImportFinished(ImportOperation op) {
        if (op != null) {
            ws.convertAndSend("/topic/imports", op);
        }
    }
}
