package ru.practicum.ewm.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.StatsMapper;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatsServiceImpl implements StatsService {
    StatsRepository repository;

    @Autowired
    public StatsServiceImpl(StatsRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = StatsMapper.toEndpointHit(endpointHitDto);
        EndpointHit endpointHitFromRepository = repository.save(endpointHit);
        log.info(String.format("Данные о запросе пользователя сохранены в базе: id # %d.", endpointHitFromRepository.getId()));
        return StatsMapper.toEndpointHitDto(endpointHitFromRepository);
    }

    @Override
    public List<ViewStatsDto> getViewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            if (uris != null) {
                return repository.getViewStatsWithUniqueIpForUriList(start, end, uris);
            } else {
                return repository.getViewStatsWithUniqueIpWithoutUriList(start, end);
            }
        } else {
            if (uris != null) {
                return repository.getAllViewStatsForUriLIst(start, end, uris);
            } else {
                return repository.getAllViewStatsWithoutUriList(start, end);
            }
        }
    }
}