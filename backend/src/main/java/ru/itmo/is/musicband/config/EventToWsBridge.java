package ru.itmo.is.musicband.config;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.dto.BandEvent;
import ru.itmo.is.musicband.service.BandChangedEvent;

@Component
@RequiredArgsConstructor
public class EventToWsBridge {
    private final SimpMessagingTemplate ws;

    @TransactionalEventListener
    public void onBandChanged(BandChangedEvent e) {
        ws.convertAndSend("/topic/bands", new BandEvent(e.type(), e.id()));
    }
}
