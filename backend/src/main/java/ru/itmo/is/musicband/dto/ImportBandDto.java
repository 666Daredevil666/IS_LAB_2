package ru.itmo.is.musicband.dto;

import java.time.LocalDate;

public record ImportBandDto(
        String bandName,
        String bandGenre,
        Long bandNumberOfParticipants,
        Long bandSinglesCount,
        String bandDescription,
        Long bandAlbumsCount,
        Double coordX,
        Double coordY,
        String albumName,
        Long albumSales,
        String frontName,
        String frontEyeColor,
        String frontHairColor,
        String frontCountry,
        Long frontWeight,
        LocalDate frontBirthday,
        Long locX,
        Float locY,
        String locName
) {
}
