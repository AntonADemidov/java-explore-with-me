package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.ewm.ViewStatsDto(h.app, h.uri, count(distinct h.ip) as c) " +
            "from EndpointHit as h " +
            "where (h.timestamp >= ?1 " +
            "and h.timestamp <= ?2 " +
            "and h.uri IN ?3) " +
            "group by h.app, h.uri " +
            "order by c desc")
    List<ViewStatsDto> getViewStatsWithUniqueIpForUriList(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "select h.app, h.uri, count(distinct h.ip) as c " +
            "from hits as h " +
            "where (h.timestamp >= ?1 " +
            "and h.timestamp <= ?2) " +
            "group by h.app, h.uri " +
            "order by c desc", nativeQuery = true)
    List<ViewStatsDto> getViewStatsWithUniqueIpWithoutUriList(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ewm.ViewStatsDto(h.app, h.uri, count(h.ip) as c) " +
            "from EndpointHit as h " +
            "where (h.timestamp >= ?1 " +
            "and h.timestamp <= ?2 " +
            "and h.uri IN ?3) " +
            "group by h.app, h.uri " +
            "order by c desc")
    List<ViewStatsDto> getAllViewStatsForUriLIst(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ewm.ViewStatsDto(h.app, h.uri, count(h.ip) as c) " +
            "from EndpointHit as h " +
            "where (h.timestamp >= ?1 " +
            "and h.timestamp <= ?2) " +
            "group by h.app, h.uri " +
            "order by c desc")
    List<ViewStatsDto> getAllViewStatsWithoutUriList(LocalDateTime start, LocalDateTime end);
}