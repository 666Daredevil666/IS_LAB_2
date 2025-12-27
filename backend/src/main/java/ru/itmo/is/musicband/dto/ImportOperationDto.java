package ru.itmo.is.musicband.dto;

import java.time.LocalDateTime;

public record ImportOperationDto(
        Long id,
        String userName,
        String status,
        Integer addedCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

