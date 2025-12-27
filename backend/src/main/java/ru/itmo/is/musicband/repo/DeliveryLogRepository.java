package ru.itmo.is.musicband.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.musicband.domain.DeliveryLog;

import java.util.Optional;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
    Optional<DeliveryLog> findByEventId(Long eventId);
}

