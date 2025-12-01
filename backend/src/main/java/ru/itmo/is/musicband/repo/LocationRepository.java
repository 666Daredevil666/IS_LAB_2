package ru.itmo.is.musicband.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.musicband.domain.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
