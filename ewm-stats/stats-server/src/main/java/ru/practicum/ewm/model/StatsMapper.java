package ru.practicum.ewm.model;

import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.EndpointHitDtoFromUser;

import java.time.LocalDateTime;

public class StatsMapper {
    public static EndpointHitDto toEndpointHitDto (EndpointHitDtoFromUser endpointHitDtoFromUser, LocalDateTime timestamp) {
        return new EndpointHitDto(endpointHitDtoFromUser.getId(), endpointHitDtoFromUser.getApp(),
                endpointHitDtoFromUser.getUri(), endpointHitDtoFromUser.getIp(), timestamp);
    }

    public static EndpointHitDto toEndpointHitDto (EndpointHit endpointHit) {
        return new EndpointHitDto(endpointHit.getId(), endpointHit.getApp(), endpointHit.getUri(), endpointHit.getIp(),
                endpointHit.getTimestamp());
    }

    public static EndpointHit toEndpointHit (EndpointHitDto endpointHitDto) {
        return new EndpointHit(endpointHitDto.getId(), endpointHitDto.getApp(), endpointHitDto.getUri(),
                endpointHitDto.getIp(), endpointHitDto.getTimestamp());
    }
}