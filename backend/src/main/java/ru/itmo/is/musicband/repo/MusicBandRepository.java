package ru.itmo.is.musicband.repo;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ru.itmo.is.musicband.domain.*;

import java.util.List;

@Repository
public interface MusicBandRepository extends JpaRepository<MusicBand, Long>, JpaSpecificationExecutor<MusicBand> {
    int deleteByAlbumsCount(long albumsCount);

    List<MusicBand> findByDescriptionStartingWith(String prefix);

    List<MusicBand> findByGenre(MusicGenre genre);
}
