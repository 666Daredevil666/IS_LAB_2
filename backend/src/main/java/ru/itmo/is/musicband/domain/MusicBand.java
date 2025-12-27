package ru.itmo.is.musicband.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "music_band")
@Getter
@Setter
public class MusicBand {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mb_seq")
    @SequenceGenerator(name = "mb_seq", sequenceName = "musicband_seq", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Embedded
    @Valid
    private Coordinates coordinates;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @PrePersist
    void prePersist() {
        this.creationDate = LocalDateTime.now();
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MusicGenre genre;

    @Positive
    private Long numberOfParticipants;

    @Positive
    @Column(nullable = false)
    private int singlesCount;

    @NotNull
    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "best_album_id")
    private Album bestAlbum;

    @Positive
    @Column(nullable = false)
    private long albumsCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "front_man_id", nullable = false)
    private Person frontMan;

    @Version
    private Long version;
}
