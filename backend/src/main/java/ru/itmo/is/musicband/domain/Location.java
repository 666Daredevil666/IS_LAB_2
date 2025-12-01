package ru.itmo.is.musicband.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private long x;
    @NotNull
    @Column(nullable = false)
    private Float y;
    @NotNull
    @Size(max = 205)
    @Column(nullable = false, length = 205)
    private String name;
}
