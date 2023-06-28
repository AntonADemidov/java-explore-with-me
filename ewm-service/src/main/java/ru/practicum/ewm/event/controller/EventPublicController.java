package ru.practicum.ewm.event.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.model.EventFullDto;
import ru.practicum.ewm.event.model.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventPublicController {
    static final String ID = "/{id}";
    EventService eventService;

    @GetMapping
    public List<EventShortDto> getPublicEvents(@RequestParam(value = "text", required = false) String text,
                                               @RequestParam(value = "categories", required = false) List<Long> categories,
                                               @RequestParam(value = "paid", required = false) Boolean paid,
                                               @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                               @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                               @RequestParam(value = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(value = "sort", required = false, defaultValue = "EVENT_DATE") String sort,
                                               @RequestParam(value = "from", required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(value = "size", required = false, defaultValue = "10") @PositiveOrZero Integer size,
                                               HttpServletRequest request) {
        log.info("Получение списка событий по параметрам: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}.",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        log.info("IP-адрес пользователя: ip={}.", request.getRemoteAddr());
        log.info("Эндпойнт: endpoint={}.", request.getRequestURI());
        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping(ID)
    public EventFullDto getPublicEventById(@PathVariable @Positive Long id, HttpServletRequest request) {
        log.info("Получение события: eventId={}.", id);
        log.info("IP-адрес пользователя: ip={}.", request.getRemoteAddr());
        log.info("Эндпойнт: endpoint={}.", request.getRequestURI());
        return eventService.getPublicEventById(id, request);
    }
}