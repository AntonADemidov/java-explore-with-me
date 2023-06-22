package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.model.EventFullDto;
import ru.practicum.ewm.event.model.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventPublicController {
    EventService eventService;

    //TODO
    @GetMapping
    public List<EventFullDto> getPublicEvents(@RequestParam(value = "text", required = false) String text,
                                               @RequestParam(value = "categories", required = false) List<Long> categories,
                                               @RequestParam(value = "paid", required = false) Boolean paid,
                                               @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                               @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                               @RequestParam(value = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(value = "sort", required = false, defaultValue = "EVENT_DATE") String sort,
                                               @RequestParam(value = "from", required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(value = "size", required = false, defaultValue = "10") @PositiveOrZero Integer size) {
        log.info("Получение списка событий по параметрам: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}.",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    //TODO
    @GetMapping("/{id}")
    public EventFullDto getPublicEventById(@PathVariable @Positive Long eventId) {
        log.info("Получение события: eventId={}.", eventId);
        return eventService.getPublicEventById(eventId);
    }
}