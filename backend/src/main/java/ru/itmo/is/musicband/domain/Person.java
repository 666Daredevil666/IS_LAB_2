package ru.itmo.is.musicband.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    private Color eyeColor;
    @Enumerated(EnumType.STRING)
    private Color hairColor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
    @NotNull
    @Column(nullable = false)
    private LocalDate birthday;
    @Positive
    @Column
    private Long weight;
    @Enumerated(EnumType.STRING)
    private Country nationality;
}
