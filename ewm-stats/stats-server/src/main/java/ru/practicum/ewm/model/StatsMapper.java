package ru.practicum.ewm.model;

import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.EndpointHitFromUserDto;

public class StatsMapper {
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
        endpointHit.setTimestamp(endpointHitFromUserDto.getTimestamp());
        return endpointHit;
    }
}