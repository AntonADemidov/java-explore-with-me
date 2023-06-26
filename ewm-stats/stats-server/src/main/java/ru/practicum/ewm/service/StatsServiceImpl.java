package ru.practicum.ewm.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.EndpointHitFromUserDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.StatsMapper;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatsServiceImpl implements StatsService {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    StatsRepository repository;

    @Transactional
    @Override
    public EndpointHitDto createEndpointHit(EndpointHitFromUserDto endpointHitFromUserDto) {
        EndpointHit endpointHit = StatsMapper.toEndpointHit(endpointHitFromUserDto);
        EndpointHit savedEndPointHit = repository.save(endpointHit);
        log.info(String.format("Данные о запросе пользователя сохранены в базе: id # %d.", savedEndPointHit.getId()));
        return StatsMapper.toEndpointHitDto(savedEndPointHit);
    }

    @Override
    public List<ViewStatsDto> getViewStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime startTime = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(end, FORMATTER);

        List<ViewStatsDto> list;
        if (unique) {
            if (uris != null) {
                list = repository.getViewStatsWithUniqueIpForUriList(startTime, endTime, uris);
            } else {
                list = repository.getViewStatsWithUniqueIpWithoutUriList(startTime, endTime);
            }
        } else {
            if (uris != null) {
                list = repository.getAllViewStatsForUriLIst(startTime, endTime, uris);
            } else {
                list = repository.getAllViewStatsWithoutUriList(startTime, endTime);
            }
        }
        return list;
    }
}