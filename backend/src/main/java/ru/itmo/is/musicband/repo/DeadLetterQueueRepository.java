package ru.itmo.is.musicband.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.is.musicband.domain.DeadLetterQueue;

public interface DeadLetterQueueRepository extends JpaRepository<DeadLetterQueue, Long> {
}

