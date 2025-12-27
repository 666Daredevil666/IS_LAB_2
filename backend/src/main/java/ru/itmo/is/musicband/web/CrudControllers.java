package ru.itmo.is.musicband.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.musicband.dto.*;
import ru.itmo.is.musicband.mapper.*;
import ru.itmo.is.musicband.repo.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CrudControllers {
    private final AlbumRepository albumRepo;
    private final PersonRepository personRepo;
    private final LocationRepository locationRepo;
    private final AlbumMapper albumMapper;
    private final PersonMapper personMapper;
    private final LocationMapper locationMapper;

    @GetMapping("/albums")
    public List<AlbumDto> albums() {
        return albumRepo.findAll().stream().map(albumMapper::toDto).toList();
    }

    @PostMapping("/albums")
    public AlbumDto addAlbum(@Valid @RequestBody AlbumDto dto) {
        var entity = albumMapper.toEntity(dto);
        if (entity != null) {
            return albumMapper.toDto(albumRepo.save(entity));
        }
        throw new IllegalArgumentException("Invalid album data");
    }

    @PutMapping("/albums/{id}")
    public AlbumDto updAlbum(@PathVariable long id, @Valid @RequestBody AlbumDto dto) {
        var e = albumRepo.findById(id).orElseThrow();
        var n = albumMapper.toEntity(dto);
        n.setId(e.getId());
        return albumMapper.toDto(albumRepo.save(n));
    }

    @DeleteMapping("/albums/{id}")
    public ResponseEntity<?> delAlbum(@PathVariable long id) {
        albumRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/persons")
    public List<PersonDto> persons() {
        return personRepo.findAll().stream().map(personMapper::toDto).toList();
    }

    @PostMapping("/persons")
    public PersonDto addPerson(@Valid @RequestBody PersonDto dto) {
        var p = personMapper.toEntity(dto);
        if (p != null) {
            return personMapper.toDto(personRepo.save(p));
        }
        throw new IllegalArgumentException("Invalid person data");
    }

    @PutMapping("/persons/{id}")
    public PersonDto updPerson(@PathVariable long id, @Valid @RequestBody PersonDto dto) {
        var e = personRepo.findById(id).orElseThrow();
        var n = personMapper.toEntity(dto);
        n.setId(e.getId());
        return personMapper.toDto(personRepo.save(n));
    }

    @DeleteMapping("/persons/{id}")
    public ResponseEntity<?> delPerson(@PathVariable long id) {
        personRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locations")
    public List<LocationDto> locations() {
        return locationRepo.findAll().stream().map(locationMapper::toDto).toList();
    }

    @PostMapping("/locations")
    public LocationDto addLocation(@Valid @RequestBody LocationDto dto) {
        var entity = locationMapper.toEntity(dto);
        if (entity != null) {
            return locationMapper.toDto(locationRepo.save(entity));
        }
        throw new IllegalArgumentException("Invalid location data");
    }

    @PutMapping("/locations/{id}")
    public LocationDto updLocation(@PathVariable long id, @Valid @RequestBody LocationDto dto) {
        var e = locationRepo.findById(id).orElseThrow();
        var n = locationMapper.toEntity(dto);
        n.setId(e.getId());
        return locationMapper.toDto(locationRepo.save(n));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<?> delLocation(@PathVariable long id) {
        locationRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
