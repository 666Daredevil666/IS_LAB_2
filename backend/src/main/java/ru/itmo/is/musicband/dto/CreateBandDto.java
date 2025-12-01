package ru.itmo.is.musicband.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.itmo.is.musicband.domain.MusicGenre;

@Data
public class CreateBandDto {
    @NotBlank
    private String name;
    @NotNull
    @Valid
    private CoordinatesDto coordinates;
    @NotNull
    private MusicGenre genre;
    @Positive
    private Long numberOfParticipants;
    @Positive
    private Integer singlesCount;
    @NotNull
    private String description;
    private Long bestAlbumId;
    @Positive
    private Long albumsCount;
    @NotNull
    private Long frontManId;
}
