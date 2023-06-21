package ru.practicum.ewm.controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.EndpointHitFromUserDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.model.StatsMapper;
import ru.practicum.ewm.service.StatsService;
import ru.practicum.ewm.service.StatsServiceImpl;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Validated
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatsController {
    StatsService statsService;
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsController(StatsServiceImpl statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    public EndpointHitDto createEndpointHit(@RequestBody @Valid EndpointHitFromUserDto endpointHitFromUserDto) {
        LocalDateTime timestamp = LocalDateTime.parse(endpointHitFromUserDto.getTimestamp(), FORMATTER);
        EndpointHitDto endpointHitDto = StatsMapper.toEndpointHitDto(endpointHitFromUserDto, timestamp);
        log.info("Сохранение данных о запросе пользователя {}", endpointHitDto);
        return statsService.createEndpointHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getViewStats(@RequestParam String start, @RequestParam String end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        LocalDateTime startTime = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(end, FORMATTER);
        log.info("Получение статистики о запросах пользователей: start={}, end={}, unique={}, uris={}",
                startTime, endTime, unique, uris);
        return statsService.getViewStats(startTime, endTime, uris, unique);
    }
}