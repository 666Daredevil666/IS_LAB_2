package ru.itmo.is.musicband.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.itmo.is.musicband.domain.Color;
import ru.itmo.is.musicband.domain.Country;

import java.time.LocalDate;

@Data
public class PersonDto {
    private Long id;
    @NotBlank
    private String name;
    private Color eyeColor;
    private Color hairColor;
    private Long locationId;
    @NotNull
    private LocalDate birthday;
    @Positive
    private Long weight;
    private Country nationality;
}
