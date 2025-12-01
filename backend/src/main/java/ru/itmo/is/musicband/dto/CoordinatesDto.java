package ru.itmo.is.musicband.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoordinatesDto {
    @NotNull
    @DecimalMax(value = "231", inclusive = true)
    private Float x;
    @NotNull
    @DecimalMax(value = "879", inclusive = true)
    private Double y;
}
