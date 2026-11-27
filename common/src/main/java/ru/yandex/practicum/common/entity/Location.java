package ru.yandex.practicum.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Широта и долгота места проведения события
 */
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Builder
public class Location {
    @Column(name = "lat")
    private Float lat;

    @Column(name = "lon")
    private Float lon;
}
