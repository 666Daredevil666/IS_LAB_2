package ru.itmo.is.musicband.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.musicband.domain.MusicBand;
import ru.itmo.is.musicband.repo.AlbumRepository;
import ru.itmo.is.musicband.repo.MusicBandRepository;

@Service
@RequiredArgsConstructor
public class UniquenessService {
    private final MusicBandRepository bandRepo;
    private final AlbumRepository albumRepo;

    @Transactional(readOnly = true)
    public void ensureBandUnique(MusicBand band) {
        try {
            var existing = bandRepo.findByNameAndGenre(band.getName(), band.getGenre());
            if (existing.isPresent() && (band.getId() == null || !existing.get().getId().equals(band.getId()))) {
                throw new IllegalStateException("Band with same name and genre already exists");
            }
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            throw new IllegalStateException("Band with same name and genre already exists (duplicate found)");
        }
    }

    @Transactional(readOnly = true)
    public void ensureAlbumNameUnique(String albumName, Long currentId) {
        if (albumName == null || albumName.isBlank()) return;
        try {
            var existing = albumRepo.findByName(albumName);
            if (existing.isPresent() && (currentId == null || !existing.get().getId().equals(currentId))) {
                throw new IllegalStateException("Album with same name already exists");
            }
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            throw new IllegalStateException("Album with same name already exists (duplicate found)");
        }
    }
}

