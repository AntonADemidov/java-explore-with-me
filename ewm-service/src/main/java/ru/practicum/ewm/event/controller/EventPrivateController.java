package ru.practicum.ewm.event.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventPrivateController {
    EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @Positive Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Создание нового события {} пользователем с userId={}.", newEventDto.getTitle(), userId);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getEventsByOwner(@PathVariable @Positive Long userId,
                                                @RequestParam(value = "from", required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(value = "size", required = false, defaultValue = "10") @PositiveOrZero Integer size) {
        log.info("Получение списка событий пользователя: userId={}, from={}, size={}.", userId, from, size);
        return eventService.getEventsByOwner(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByOwner(@PathVariable @Positive Long userId,
                                        @PathVariable @Positive Long eventId) {
        log.info("Просмотр события пользователя: userId={}, eventId={}", userId, eventId);
        return eventService.getEventByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(@PathVariable @Positive Long userId,
                                           @PathVariable @Positive Long eventId,
                                           @RequestBody @Valid UpdateEventRequest updateEventRequest) {
        log.info("Обновление события пользователем: eventId={}, userId={}.", eventId, userId);
        return eventService.updateEventByOwner(userId, eventId, updateEventRequest);
    }
}