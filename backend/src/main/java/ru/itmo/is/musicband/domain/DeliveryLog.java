package ru.itmo.is.musicband.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DeliveryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, unique = true)
    private String idempotencyToken;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int attempts;

    private String lastError;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    private LocalDateTime deliveredAt;

    @PrePersist
    void prePersist() {
        receivedAt = LocalDateTime.now();
    }
}

