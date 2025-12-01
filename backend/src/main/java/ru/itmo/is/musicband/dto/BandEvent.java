package ru.itmo.is.musicband.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BandEvent {
    private String type;
    private Long id;
}
