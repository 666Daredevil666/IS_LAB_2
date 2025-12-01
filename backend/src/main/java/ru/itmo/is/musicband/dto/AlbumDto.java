package ru.itmo.is.musicband.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AlbumDto {
    private Long id;
    @NotBlank
    private String name;
    @Positive
    private Integer sales;
}
