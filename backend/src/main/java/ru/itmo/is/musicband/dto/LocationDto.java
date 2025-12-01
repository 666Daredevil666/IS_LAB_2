package ru.itmo.is.musicband.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LocationDto {
    private Long id;
    @NotNull
    private Long x;
    @NotNull
    private Float y;
    @NotNull
    @Size(max = 205)
    private String name;
}
