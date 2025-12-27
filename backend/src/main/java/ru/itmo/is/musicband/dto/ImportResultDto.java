package ru.itmo.is.musicband.dto;

public record ImportResultDto(Long operationId, String status, Integer addedCount, String errorMessage) {
}

