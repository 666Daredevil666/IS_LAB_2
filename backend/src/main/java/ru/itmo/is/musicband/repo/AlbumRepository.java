package ru.itmo.is.musicband.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.musicband.domain.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
}
