package ru.itmo.is.musicband.repo;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.is.musicband.domain.OutboxEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEvent e where e.status = 'NEW' and (e.nextRunAt is null or e.nextRunAt <= :now) order by e.id")
    List<OutboxEvent> pickBatch(@Param("now") LocalDateTime now, Pageable pageable);
}
