package ru.itmo.is.musicband.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.musicband.domain.*;
import ru.itmo.is.musicband.dto.ImportBandDto;
import ru.itmo.is.musicband.dto.ImportResultDto;
import ru.itmo.is.musicband.repo.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final MusicBandRepository bandRepo;
    private final AlbumRepository albumRepo;
    private final PersonRepository personRepo;
    private final LocationRepository locationRepo;
    private final ImportOperationRepository importOpRepo;
    private final UniquenessService uniq;
    private final OutboxHelper outboxHelper;

    @Transactional
    public ImportResultDto importCsv(MultipartFile file, String username) throws Exception {
        var op = new ImportOperation();
        op.setUserName(username);
        op.setStatus("NEW");
        importOpRepo.save(op);

        try {
            List<ImportBandDto> rows = parse(file);
            var entities = toEntities(rows);

            var seen = new HashSet<String>();
            for (MusicBand b : entities) {
                String key = b.getName() + "|" + b.getGenre();
                if (!seen.add(key)) {
                    throw new IllegalStateException("Duplicate band in file: " + key);
                }
            }

            for (MusicBand b : entities) {
                uniq.ensureBandUnique(b);
                if (b.getBestAlbum() != null) {
                    uniq.ensureAlbumNameUnique(b.getBestAlbum().getName(), null);
                }
            }

            List<Location> locations = new ArrayList<>();
            List<Person> persons = new ArrayList<>();
            List<Album> albums = new ArrayList<>();

            for (MusicBand b : entities) {
                if (b.getFrontMan().getLocation() != null) {
                    locations.add(b.getFrontMan().getLocation());
                }
                persons.add(b.getFrontMan());
                if (b.getBestAlbum() != null) {
                    albums.add(b.getBestAlbum());
                }
            }

            locationRepo.saveAll(locations);
            personRepo.saveAll(persons);
            albumRepo.saveAll(albums);
            bandRepo.saveAll(entities);

            for (MusicBand b : entities) {
                outboxHelper.writeOutbox(b, "CREATED");
            }

            op.setStatus("SUCCESS");
            op.setAddedCount(entities.size());
            importOpRepo.save(op);
            outboxHelper.publishImport(op);
            return new ImportResultDto(op.getId(), op.getStatus(), op.getAddedCount(), null);
        } catch (Exception ex) {
            op.setStatus("FAILED");
            op.setErrorMessage(ex.getMessage());
            importOpRepo.save(op);
            outboxHelper.publishImport(op);
            throw ex;
        }
    }

    private List<ImportBandDto> parse(MultipartFile file) throws Exception {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             var parser = CSVParser.parse(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {
            List<ImportBandDto> res = new ArrayList<>();
            for (CSVRecord r : parser) {
                res.add(new ImportBandDto(
                        r.get("band.name"),
                        r.get("band.genre"),
                        parseLong(r.get("band.numberOfParticipants")),
                        parseLong(r.get("band.singlesCount")),
                        r.get("band.description"),
                        parseLong(r.get("band.albumsCount")),
                        parseDouble(r.get("coord.x")),
                        parseDouble(r.get("coord.y")),
                        emptyToNull(r.get("album.name")),
                        parseLong(r.get("album.sales")),
                        emptyToNull(r.get("front.name")),
                        emptyToNull(r.get("front.eyeColor")),
                        emptyToNull(r.get("front.hairColor")),
                        emptyToNull(r.get("front.country")),
                        parseLong(r.get("front.weight")),
                        parseDate(r.get("front.birthday")),
                        parseLong(r.get("loc.x")),
                        parseFloat(r.get("loc.y")),
                        emptyToNull(r.get("loc.name"))
                ));
            }
            return res;
        }
    }

    private List<MusicBand> toEntities(List<ImportBandDto> rows) {
        List<MusicBand> list = new ArrayList<>();
        for (ImportBandDto r : rows) {
            requireNotNull(r.bandName(), "band.name");
            requireNotNull(r.bandGenre(), "band.genre");
            requireNotNull(r.bandSinglesCount(), "band.singlesCount");
            requireNotNull(r.bandDescription(), "band.description");
            requireNotNull(r.bandAlbumsCount(), "band.albumsCount");
            requireNotNull(r.coordX(), "coord.x");
            requireNotNull(r.coordY(), "coord.y");
            requireNotNull(r.frontName(), "front.name");
            requireNotNull(r.frontBirthday(), "front.birthday");
            requireNotNull(r.frontWeight(), "front.weight");

            var band = new MusicBand();
            band.setName(r.bandName());
            band.setGenre(MusicGenre.valueOf(r.bandGenre()));
            band.setNumberOfParticipants(r.bandNumberOfParticipants());
            band.setSinglesCount(Math.toIntExact(r.bandSinglesCount()));
            band.setDescription(r.bandDescription());
            band.setAlbumsCount(r.bandAlbumsCount());

            var coords = new Coordinates();
            coords.setX(r.coordX().floatValue());
            coords.setY(r.coordY());
            band.setCoordinates(coords);

            if (r.albumName() != null) {
                var album = new Album();
                album.setName(r.albumName());
                album.setSales(Math.toIntExact(r.albumSales()));
                band.setBestAlbum(album);
            }

            var person = new Person();
            person.setName(r.frontName());
            if (r.frontEyeColor() != null) person.setEyeColor(Color.valueOf(r.frontEyeColor()));
            if (r.frontHairColor() != null) person.setHairColor(Color.valueOf(r.frontHairColor()));
            if (r.frontCountry() != null) person.setNationality(Country.valueOf(r.frontCountry()));
            person.setWeight(r.frontWeight());
            person.setBirthday(r.frontBirthday());

            if (r.locX() != null || r.locY() != null || r.locName() != null) {
                requireNotNull(r.locX(), "loc.x");
                requireNotNull(r.locY(), "loc.y");
                requireNotNull(r.locName(), "loc.name");
                var loc = new Location();
                loc.setX(r.locX() == null ? 0 : r.locX());
                loc.setY(r.locY());
                loc.setName(r.locName());
                person.setLocation(loc);
            }

            band.setFrontMan(person);
            list.add(band);
        }
        return list;
    }

    private Long parseLong(String v) {
        return (v == null || v.isBlank()) ? null : Long.parseLong(v);
    }

    private Double parseDouble(String v) {
        return (v == null || v.isBlank()) ? null : Double.parseDouble(v);
    }

    private Float parseFloat(String v) {
        return (v == null || v.isBlank()) ? null : Float.parseFloat(v);
    }

    private LocalDate parseDate(String v) {
        return (v == null || v.isBlank()) ? null : LocalDate.parse(v);
    }

    private String emptyToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    private void requireNotNull(Object v, String field) {
        if (v == null) {
            throw new IllegalArgumentException("Field " + field + " is required");
        }
    }
}

