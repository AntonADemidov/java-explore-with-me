package ru.practicum.ewm.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.DateValidationException;
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

    @Override
    @Transactional
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
        validateDates(startTime, endTime);

        if (unique) {
            if (uris != null) {
                return repository.getViewStatsWithUniqueIpForUriList(startTime, endTime, uris);
            } else {
                return repository.getViewStatsWithUniqueIpWithoutUriList(startTime, endTime);
            }
        } else {
            if (uris != null) {
                return repository.getAllViewStatsForUriLIst(startTime, endTime, uris);
            } else {
                return repository.getAllViewStatsWithoutUriList(startTime, endTime);
            }
        }
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new DateValidationException(String.format("Дата начала диапазона start=%s не может быть позднее даты окончания диапазона end=%s",
                    start, end));
        }
    }
}