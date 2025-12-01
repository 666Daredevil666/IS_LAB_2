package ru.itmo.is.musicband.dto;

import lombok.Data;

@Data
public class BandFilters {
    private String nameEquals;
    private String descriptionEquals;
    private String albumNameEquals;
    private String frontManNameEquals;
    private String genreEquals;
}
