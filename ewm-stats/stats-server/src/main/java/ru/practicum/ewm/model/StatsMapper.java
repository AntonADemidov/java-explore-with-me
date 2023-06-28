package ru.practicum.ewm.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.EndpointHitFromUserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatsMapper {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setId(endpointHit.getId());
        endpointHitDto.setApp(endpointHit.getApp());
        endpointHitDto.setUri(endpointHit.getUri());
        endpointHitDto.setIp(endpointHit.getIp());
        endpointHitDto.setTimestamp(endpointHit.getTimestamp());
        return endpointHitDto;
    }

    public static EndpointHit toEndpointHit(EndpointHitFromUserDto endpointHitFromUserDto) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(endpointHitFromUserDto.getApp());
        endpointHit.setUri(endpointHitFromUserDto.getUri());
        endpointHit.setIp(endpointHitFromUserDto.getIp());
        endpointHit.setTimestamp(LocalDateTime.parse(endpointHitFromUserDto.getTimestamp(), FORMATTER));
        return endpointHit;
    }
}