package ru.practicum.ewm.event.mapper;

import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.LocationDto;

public class LocationMapper {
    public static Location toLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return location;
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}