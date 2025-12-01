package ru.itmo.is.musicband.dto;

import lombok.Data;
import ru.itmo.is.musicband.domain.MusicGenre;

@Data
public class MusicBandDto {
    private Long id;
    private String name;
    private CoordinatesDto coordinates;
    private String creationDate;
    private MusicGenre genre;
    private Long numberOfParticipants;
    private Integer singlesCount;
    private String description;
    private Long bestAlbumId;
    private String bestAlbumName;
    private Long albumsCount;
    private Long frontManId;
    private String frontManName;
}
