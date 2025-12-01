package ru.itmo.is.musicband.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.musicband.domain.MusicGenre;
import ru.itmo.is.musicband.domain.Person;
import ru.itmo.is.musicband.dto.*;
import ru.itmo.is.musicband.service.MusicBandService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/bands")
@RequiredArgsConstructor
@Validated
public class BandController {
  private final MusicBandService service;

  @GetMapping
  public Page<MusicBandDto> list(BandFilters f,
                                 @RequestParam(name="page", defaultValue="0") int page,
                                 @RequestParam(name="size", defaultValue="10") int size,
                                 @RequestParam(name="sort", defaultValue="id,asc") String sort){
    String[] s = sort.split(",");
    Pageable p = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(s[1]), s[0]));
    return service.list(f, p);
  }

  @GetMapping("/{id}")
  public MusicBandDto get(@PathVariable long id){ return service.get(id); }

  @PostMapping
  public MusicBandDto create(@Valid @RequestBody CreateBandDto dto){ return service.create(dto); }

  @PutMapping("/{id}")
  public MusicBandDto update(@PathVariable long id, @Valid @RequestBody UpdateBandDto dto){ return service.update(id, dto); }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable long id){ service.delete(id); return ResponseEntity.noContent().build(); }

  @PostMapping("/ops/deleteByAlbumsCount")
  public ResponseEntity<?> deleteByAlbums(@RequestParam(name="value") long value){ return ResponseEntity.ok().body(java.util.Map.of("deleted", service.deleteByAlbumsCount(value))); }

  @GetMapping("/search/descriptionStarts")
  public List<MusicBandDto> descStarts(@RequestParam(name="prefix") String prefix){ return service.descriptionStarts(prefix); }

  @GetMapping("/search/byGenre")
  public List<MusicBandDto> byGenre(@RequestParam(name="genre") MusicGenre genre){ return service.byGenre(genre); }

  @PostMapping("/{id}/participants/decrement")
  public MusicBandDto decrement(@PathVariable long id, @RequestParam(name="by", defaultValue="1") int by){ return service.decrementParticipants(id, by); }

  @PostMapping("/search/frontmanLessThan")
  public List<MusicBandDto> frontmanLess(@RequestBody PersonDto dto){
    Person p = new Person();
    p.setId(dto.getId());
    p.setName(dto.getName());
    p.setEyeColor(dto.getEyeColor());
    p.setHairColor(dto.getHairColor());
    p.setBirthday(dto.getBirthday());
    p.setWeight(dto.getWeight());
    p.setNationality(dto.getNationality());
    return service.frontmanLessThan(p);
  }
}
