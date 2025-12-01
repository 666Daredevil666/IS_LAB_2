package ru.itmo.is.musicband.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Coordinates {
    @Column(name = "coord_x", nullable = false)
    private float x;
    @Column(name = "coord_y", nullable = false)
    private double y;

    @AssertTrue(message = "x must be <= 231")
    public boolean isXValid() {
        return x <= 231f;
    }

    @AssertTrue(message = "y must be <= 879")
    public boolean isYValid() {
        return y <= 879d;
    }
}
