package ru.practicum.ewm.event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Column(name = "location_lat")
    private Double lat;

    @Column(name = "location_lon")
    private Double lon;
}