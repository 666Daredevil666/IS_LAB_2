package ru.itmo.is.musicband.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.itmo.is.musicband.domain.*;
import ru.itmo.is.musicband.dto.*;
import ru.itmo.is.musicband.mapper.BandMapper;
import ru.itmo.is.musicband.repo.*;

import java.util.Comparator;
import java.util.List;

import static ru.itmo.is.musicband.service.BandSpecifications.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicBandService {
    private final MusicBandRepository repo;
    private final AlbumRepository albumRepo;
    private final PersonRepository personRepo;
    private final BandMapper mapper;
    private final ApplicationEventPublisher events;
    private final UniquenessService uniq;
    private final OutboxHelper outbox;
    private final TransactionTemplate transactionTemplate;

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
        uniq.ensureBandUnique(band);
        if (band.getBestAlbum() != null) {
            uniq.ensureAlbumNameUnique(band.getBestAlbum().getName(), band.getBestAlbum().getId());
        }
        var saved = repo.save(band);
        outbox.writeOutbox(saved, "CREATED");
        return mapper.toDto(saved);
    }

    public MusicBandDto update(long id, UpdateBandDto dto) {
        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            final int currentAttempt = attempts;
            try {
                return transactionTemplate.execute(status -> {
                    try {
        var band = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        mapper.updateEntity(dto, band);
        band.setBestAlbum(dto.getBestAlbumId() == null ? null : albumRepo.getReferenceById(dto.getBestAlbumId()));
        band.setFrontMan(personRepo.getReferenceById(dto.getFrontManId()));
                        uniq.ensureBandUnique(band);
                        if (band.getBestAlbum() != null) {
                            uniq.ensureAlbumNameUnique(band.getBestAlbum().getName(), band.getBestAlbum().getId());
                        }
        var saved = repo.save(band);
                        outbox.writeOutbox(saved, "UPDATED");
        events.publishEvent(new BandChangedEvent("UPDATED", saved.getId()));
        return mapper.toDto(saved);
                    } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                        log.warn("Optimistic locking failure inside transaction on attempt {} for band {}", currentAttempt, id);
                        throw e;
                    }
                });
            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                log.warn("Optimistic locking failure on attempt {} for band {}", attempts, id, e);
                if (attempts >= 3) {
                    throw new org.springframework.orm.ObjectOptimisticLockingFailureException(
                            "Optimistic locking failure after 3 attempts", e);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (TransactionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof org.springframework.dao.OptimisticLockingFailureException) {
                    log.warn("Optimistic locking failure (wrapped in TransactionException) on attempt {} for band {}", attempts, id);
                    if (attempts >= 3) {
                        throw new org.springframework.orm.ObjectOptimisticLockingFailureException(
                                "Optimistic locking failure after 3 attempts", cause);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("Transaction exception during update attempt {} for band {}: {}", attempts, id, e.getClass().getName(), e);
                    throw e;
                }
            } catch (RuntimeException e) {
                log.error("Unexpected exception during update attempt {} for band {}: {}", attempts, id, e.getClass().getName(), e);
                throw e;
            }
        }
        throw new RuntimeException("Update failed after 3 attempts");
    }

    public void delete(long id) {
        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            try {
                transactionTemplate.execute(status -> {
                    if (!repo.existsById(id)) return null;
        repo.deleteById(id);
                    MusicBand stub = new MusicBand();
                    stub.setId(id);
                    outbox.writeOutbox(stub, "DELETED");
        events.publishEvent(new BandChangedEvent("DELETED", id));
                    return null;
                });
                return;
            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                if (attempts >= 3) {
                    throw new org.springframework.orm.ObjectOptimisticLockingFailureException(
                            "Optimistic locking failure after 3 attempts", e);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Delete failed after 3 attempts");
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
        outbox.writeOutbox(saved, "UPDATED");
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
