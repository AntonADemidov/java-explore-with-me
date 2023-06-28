package ru.practicum.ewm.event.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Transactional(readOnly = true)
public interface EventService {
    @Transactional
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size);

    EventFullDto getEventByOwner(Long userId, Long eventId);

    Event getEventById(Long eventId);

    @Transactional
    EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventRequest updateEventRequest);

    @Transactional
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories, String rangeStart,
                                        String rangeEnd, Integer from, Integer size);

    EventFullDto getPublicEventById(Long eventId, HttpServletRequest request);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                        String rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size,
                                        HttpServletRequest request);
}