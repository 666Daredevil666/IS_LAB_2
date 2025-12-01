package ru.itmo.is.musicband.service;

import org.springframework.data.jpa.domain.Specification;
import ru.itmo.is.musicband.domain.MusicBand;
import ru.itmo.is.musicband.domain.MusicGenre;

public final class BandSpecifications {
    public static Specification<MusicBand> nameEquals(String v) {
        return v == null ? null : (r, q, cb) -> cb.equal(r.get("name"), v);
    }

    public static Specification<MusicBand> descriptionEquals(String v) {
        return v == null ? null : (r, q, cb) -> cb.equal(r.get("description"), v);
    }

    public static Specification<MusicBand> albumNameEquals(String v) {
        return v == null ? null : (r, q, cb) -> cb.equal(r.join("bestAlbum").get("name"), v);
    }

    public static Specification<MusicBand> frontManNameEquals(String v) {
        return v == null ? null : (r, q, cb) -> cb.equal(r.join("frontMan").get("name"), v);
    }

    public static Specification<MusicBand> genreEquals(String v) {
        return v == null ? null : (r, q, cb) -> cb.equal(r.get("genre"), MusicGenre.valueOf(v));
    }
}
