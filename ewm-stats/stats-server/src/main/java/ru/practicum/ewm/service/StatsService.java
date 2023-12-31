package ru.practicum.ewm.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.EndpointHitFromUserDto;
import ru.practicum.ewm.ViewStatsDto;

import java.util.List;

@Transactional(readOnly = true)
public interface StatsService {

    @Transactional
    EndpointHitDto createEndpointHit(EndpointHitFromUserDto endpointHitFromUserDto);

    List<ViewStatsDto> getViewStats(String start, String end, List<String> uris, Boolean unique);
}