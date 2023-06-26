package ru.practicum.ewm.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatsController {
    StatsService statsService;
    //static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto createEndpointHit(@RequestBody @Valid EndpointHitFromUserDto endpointHitFromUserDto) {
        //LocalDateTime timestamp = LocalDateTime.parse(endpointHitFromUserDto.getTimestamp(), FORMATTER);
        //EndpointHitDto endpointHitDto = StatsMapper.toEndpointHitDto(endpointHitFromUserDto, timestamp);
        log.info("Сохранение данных о запросе пользователя {}", endpointHitFromUserDto);
        return statsService.createEndpointHit(endpointHitFromUserDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getViewStats(@RequestParam String start,
                                           @RequestParam String end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        //LocalDateTime startTime = LocalDateTime.parse(start, FORMATTER);
        //LocalDateTime endTime = LocalDateTime.parse(end, FORMATTER);
        log.info("Получение статистики о запросах пользователей: start={}, end={}, unique={}, uris={}",
                start, end, unique, uris);
        List<ViewStatsDto> list = statsService.getViewStats(start, end, uris, unique);
        log.info("Лист получен: {}", list);
        return list;
    }
}