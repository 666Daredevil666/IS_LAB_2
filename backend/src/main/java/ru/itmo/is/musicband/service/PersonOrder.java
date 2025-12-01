package ru.itmo.is.musicband.service;

import ru.itmo.is.musicband.domain.Person;

import java.util.Comparator;

public final class PersonOrder {
    public static Comparator<Person> comparator() {
        return Comparator
                .comparing(Person::getBirthday)
                .thenComparing(p -> p.getWeight() == null ? Long.MAX_VALUE : p.getWeight())
                .thenComparing(p -> p.getName() == null ? "" : p.getName())
                .thenComparing(p -> p.getNationality() == null ? "" : p.getNationality().name())
                .thenComparing(p -> p.getEyeColor() == null ? "" : p.getEyeColor().name())
                .thenComparing(p -> p.getHairColor() == null ? "" : p.getHairColor().name())
                .thenComparing(p -> p.getLocation() == null || p.getLocation().getName() == null ? "" : p.getLocation().getName());
    }
}
