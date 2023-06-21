package ru.practicum.ewm.model;

import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.EndpointHitFromUserDto;

import java.time.LocalDateTime;

public class StatsMapper {
    public static EndpointHitDto toEndpointHitDto(EndpointHitFromUserDto endpointHitFromUserDto, LocalDateTime timestamp) {
        return new EndpointHitDto(endpointHitFromUserDto.getId(), endpointHitFromUserDto.getApp(),
                endpointHitFromUserDto.getUri(), endpointHitFromUserDto.getIp(), timestamp);
    }

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return new EndpointHitDto(endpointHit.getId(), endpointHit.getApp(), endpointHit.getUri(), endpointHit.getIp(),
                endpointHit.getTimestamp());
    }

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        return new EndpointHit(endpointHitDto.getId(), endpointHitDto.getApp(), endpointHitDto.getUri(),
                endpointHitDto.getIp(), endpointHitDto.getTimestamp());
    }
}