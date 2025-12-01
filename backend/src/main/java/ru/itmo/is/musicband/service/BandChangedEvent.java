package ru.itmo.is.musicband.service;

public record BandChangedEvent(String type, Long id) {
}
