package ru.practicum.ewm.event.mapper;

import ru.practicum.ewm.event.Location;
import ru.practicum.ewm.event.dto.LocationDto;

public class LocationMapper {

    public static Location toLocation(LocationDto dto) {
        if (dto == null) return null;
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public static LocationDto toDto(Location location) {
        if (location == null) return null;
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
