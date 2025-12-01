package ru.itmo.is.musicband.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.musicband.domain.*;
import ru.itmo.is.musicband.dto.*;
import ru.itmo.is.musicband.mapper.BandMapper;
import ru.itmo.is.musicband.repo.*;

import java.util.Comparator;
import java.util.List;

import static ru.itmo.is.musicband.service.BandSpecifications.*;

@Service
@RequiredArgsConstructor
public class MusicBandService {
    private final MusicBandRepository repo;
    private final AlbumRepository albumRepo;
    private final PersonRepository personRepo;
    private final BandMapper mapper;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public Page<MusicBandDto> list(BandFilters f, Pageable pageable) {
        Specification<MusicBand> spec = Specification.where(nameEquals(f.getNameEquals()))
                .and(descriptionEquals(f.getDescriptionEquals()))
                .and(albumNameEquals(f.getAlbumNameEquals()))
                .and(frontManNameEquals(f.getFrontManNameEquals()))
                .and(genreEquals(f.getGenreEquals()));
        return repo.findAll(spec, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public MusicBandDto get(long id) {
        var band = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        return mapper.toDto(band);
    }

    @Transactional
    public MusicBandDto create(CreateBandDto dto) {
        var band = mapper.toEntity(dto);
        band.setBestAlbum(dto.getBestAlbumId() == null ? null : albumRepo.getReferenceById(dto.getBestAlbumId()));
        band.setFrontMan(personRepo.getReferenceById(dto.getFrontManId()));
        var saved = repo.save(band);
        events.publishEvent(new BandChangedEvent("CREATED", saved.getId()));
        return mapper.toDto(saved);
    }

    @Transactional
    public MusicBandDto update(long id, UpdateBandDto dto) {
        var band = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        mapper.updateEntity(dto, band);
        band.setBestAlbum(dto.getBestAlbumId() == null ? null : albumRepo.getReferenceById(dto.getBestAlbumId()));
        band.setFrontMan(personRepo.getReferenceById(dto.getFrontManId()));
        var saved = repo.save(band);
        events.publishEvent(new BandChangedEvent("UPDATED", saved.getId()));
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(long id) {
        if (!repo.existsById(id)) throw new RuntimeException("Not found");
        repo.deleteById(id);
        events.publishEvent(new BandChangedEvent("DELETED", id));
    }

    @Transactional
    public int deleteByAlbumsCount(long value) {
        int n = repo.deleteByAlbumsCount(value);
        if (n > 0) events.publishEvent(new BandChangedEvent("BULK", null));
        return n;
    }

    @Transactional(readOnly = true)
    public List<MusicBandDto> descriptionStarts(String prefix) {
        return repo.findByDescriptionStartingWith(prefix).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<MusicBandDto> byGenre(MusicGenre genre) {
        return repo.findByGenre(genre).stream().map(mapper::toDto).toList();
    }

    @Transactional
    public MusicBandDto decrementParticipants(long id, int by) {
        var band = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        long current = band.getNumberOfParticipants() == null ? 0 : band.getNumberOfParticipants();
        if (current < by) throw new RuntimeException("Cannot decrement below zero");
        band.setNumberOfParticipants(current - by);
        var saved = repo.save(band);
        events.publishEvent(new BandChangedEvent("UPDATED", saved.getId()));
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<MusicBandDto> frontmanLessThan(Person probe) {
        Comparator<Person> cmp = PersonOrder.comparator();
        return repo.findAll().stream()
                .filter(b -> cmp.compare(b.getFrontMan(), probe) < 0)
                .map(mapper::toDto)
                .toList();
    }
}
