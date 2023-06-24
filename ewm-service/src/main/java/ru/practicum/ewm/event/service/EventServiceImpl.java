package ru.practicum.ewm.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHitFromUserDto;
import ru.practicum.ewm.PageNumber;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.exception.DateValidationException;
import ru.practicum.ewm.event.exception.EventNotFoundException;
import ru.practicum.ewm.event.exception.EventValidationException;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventServiceImpl implements ru.practicum.ewm.event.service.EventService {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    int intervalForAdmin = 1;
    int intervalForOwner = 2;
    EventRepository eventRepository;
    LocationRepository locationRepository;
    UserService userService;
    CategoryService categoryService;
    StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userService.getUserById(userId);
        Event event = EventMapper.toEvent(newEventDto);

        validateEventDate(event, intervalForOwner);

        Location location = LocationMapper.toLocation(newEventDto.getLocation());
        Location actualLocation = locationRepository.save(location);
        Category category = categoryService.getById(newEventDto.getCategory());

        event.setState(State.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        //TODO
        event.setPublishedOn(LocalDateTime.now());
        event.setLocation(actualLocation);
        event.setCategory(category);
        event.setInitiator(user);

        Event actualEvent = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(actualEvent);
        log.info("Новое событие добавлено в базу: {} c id #{}.", eventFullDto.getTitle(), eventFullDto.getId());
        return eventFullDto;
    }

    private void validateEventDate(Event event, int interval) {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime eventDate = event.getEventDate();

        if (!current.plusHours(interval).isBefore(eventDate)) {
            throw new EventValidationException(String.format("Время до начала события от текущего момента в часах должно быть не менее, чем: %d. Начало события: %s",
                    interval, eventDate.format(FORMATTER)));
        }
    }

    @Override
    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        User user = userService.getUserById(userId);
        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        Page<Event> requestPage = eventRepository.findAllByInitiatorEquals(user, request);
        List<Event> events = requestPage.getContent();

        List<EventShortDto> eventShortDtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        log.info("Список мероприятий по запросу: {}.", eventShortDtos);
        return eventShortDtos;
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new EventNotFoundException(String.format("Событие с id #%d отсутствует в базе.", eventId)));
    }

    @Override
    public EventFullDto getEventByOwner(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventRequest request) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);

        validateEventStateIsNotPublished(event);
        validateEventDate(event, intervalForOwner);
        updateEventBasicData(event, request);
        updateStateByOwner(event, request);

        Event actualEvent = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(actualEvent);
        log.info("Пользователь с userId={} обновил событие в базе: {}, eventId= {}.", userId, eventFullDto.getTitle(), eventFullDto.getId());
        return eventFullDto;
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest request) {
        Event event = getEventById(eventId);

        validateEventDate(event, intervalForAdmin);
        updateEventBasicData(event, request);
        updateStateByAdmin(event, request);

        Event actualEvent = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(actualEvent);
        log.info("Администратор обновил событие в базе: {}, eventId= {}.", eventFullDto.getTitle(), eventFullDto.getId());
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start;
        LocalDateTime end;
        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        if (users != null) {
            for (Long userId : users) {
                conditions.add(event.initiator.id.eq(userId));
            }
        }

        if (states != null) {
            for (String state : states) {
                State eventState = State.valueOf(state);
                conditions.add(event.state.eq(eventState));
            }
        }

        if (categories != null) {
            for (Long categoryId : categories) {
                conditions.add(event.category.id.eq(categoryId));
            }
        }

        /*if (rangeStart != null) {
            LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));
        }

        if (rangeEnd != null) {
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            conditions.add((event.eventDate.eq(end)).or(event.eventDate.lt(end)));
        }*/

        if ((rangeStart != null) && (rangeEnd != null)) {
            start = LocalDateTime.parse(rangeStart, FORMATTER);
            end = LocalDateTime.parse(rangeEnd, FORMATTER);
            dateValidation(start, end);

            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));
            conditions.add((event.eventDate.eq(end)).or(event.eventDate.lt(end)));
        } else if ((rangeStart != null) && (rangeEnd == null)) {
            start = LocalDateTime.parse(rangeStart, FORMATTER);
            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));
        } else {
            end = LocalDateTime.parse(rangeEnd, FORMATTER);
            conditions.add((event.eventDate.eq(end)).or(event.eventDate.lt(end)));
        }

        Page<Event> requestPage;
        if (conditions.size() != 0) {
            BooleanExpression finalCondition = conditions.stream()
                    .reduce(BooleanExpression::and)
                    .get();
            requestPage = eventRepository.findAll(finalCondition, request);
        } else {
            requestPage = eventRepository.findAll(request);
        }

        List<Event> events = requestPage.getContent();

        List<EventFullDto> eventFullDtos = events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

        log.info("Список событий сформирован: количество элементов={}.", eventFullDtos.size());
        return eventFullDtos;
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId) {
        Event event = getEventById(eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new EventNotFoundException(String.format("Меропритие с eventId=%d не опубликовано", event.getId()));
        }

        /*EndpointHitFromUserDto endpointHitFromUserDto = new EndpointHitFromUserDto();
        endpointHitFromUserDto.setApp("ewm-service");
        endpointHitFromUserDto.setUri(String.format("/events/%d", event.getId()));
        endpointHitFromUserDto.setIp("192.163.0.1");
        endpointHitFromUserDto.setTimestamp(LocalDateTime.now());

        ResponseEntity<Object> response = statsClient.createEndpointHit(endpointHitFromUserDto);
        System.out.println(response);*/

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        log.info("Просмотр события по eventId={}.", eventFullDto.getId());
        return eventFullDto;
    }

    private void dateValidation (LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new DateValidationException(String.format("Дата начала диапазона start=%s не может быть позднее даты окончания диапазона end=%s",
                    start, end));
        }
    }


    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                              String rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size) {
        LocalDateTime start;
        LocalDateTime end;
        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        List<BooleanExpression> conditions = new ArrayList<>();
        QEvent event = QEvent.event;

        conditions.add(event.state.eq(State.PUBLISHED));

        if (text != null) {
            String anyText = "%";
            String condition = String.format("%s%s%s", anyText, text, anyText);
            conditions.add((event.annotation.likeIgnoreCase(condition)).or(event.description.likeIgnoreCase(condition)));
        }

        if (categories != null) {
            for (Long categoryId : categories) {
                conditions.add(event.category.id.eq(categoryId));
            }
        }

        if (paid != null) {
            conditions.add(event.paid.eq(paid));
        }

        if ((rangeStart != null) && (rangeEnd != null)) {
            start = LocalDateTime.parse(rangeStart, FORMATTER);
            end = LocalDateTime.parse(rangeEnd, FORMATTER);
            dateValidation(start, end);

            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));
            conditions.add((event.eventDate.eq(end)).or(event.eventDate.lt(end)));

        } else if ((rangeStart != null) && (rangeEnd == null)) {
            start = LocalDateTime.parse(rangeStart, FORMATTER);
            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));

        } else if ((rangeStart == null) && (rangeEnd != null)) {
            end = LocalDateTime.parse(rangeEnd, FORMATTER);
            conditions.add((event.eventDate.eq(end)).or(event.eventDate.lt(end)));

        } else {
            start = LocalDateTime.now();
            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));
        }

        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();

        EventSort finalSort = EventSort.valueOf(sort);
        Comparator<EventShortDto> comparator;
        if (finalSort.equals(EventSort.EVENT_DATE)) {
            comparator = Comparator.comparing(EventShortDto::getEventDate);
        } else {
            comparator = Comparator.comparing(EventShortDto::getViews);
        }

        Page<Event> requestPage = eventRepository.findAll(finalCondition, request);

        List<EventShortDto> events;
        if (onlyAvailable) {
            events = requestPage.getContent().stream()
                    .filter(ev -> ev.getParticipantLimit() != 0)
                    .filter(ev -> ev.getParticipationRequests().stream()
                            .filter(req -> req.getStatus().equals(RequestState.CONFIRMED))
                            .count() < ev.getParticipantLimit())
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
        } else {
            events = requestPage.getContent().stream()
                    .map(EventMapper::toEventShortDto)
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        log.info("Список событий сформирован: количество элементов={}.", events.size());
        return events;
    }

    private void updateStateByOwner(Event event, UpdateEventRequest request) {
        if (request.getStateAction() != null) {
            StateActionOfOwner stateAction = StateActionOfOwner.valueOf(request.getStateAction());

            if (stateAction.equals(StateActionOfOwner.SEND_TO_REVIEW)) {
                event.setState(State.PENDING);
            } else {
                event.setState(State.CANCELED);
            }
        }
    }

    private void updateStateByAdmin(Event event, UpdateEventRequest request) {
        if (request.getStateAction() != null) {
            StateActionOfAdmin stateAction = StateActionOfAdmin.valueOf(request.getStateAction());

            if (stateAction.equals(StateActionOfAdmin.PUBLISH_EVENT)) {
                validateEventStateIsNotPublished(event);
                validateEventStateIsNotCancelled(event);
                event.setState(State.PUBLISHED);
                event.setCreatedOn(LocalDateTime.now());
            } else {
                validateEventStateIsNotPublished(event);
                validateEventStateIsNotCancelled(event);
                event.setState(State.CANCELED);
            }
        }
    }

    private void validateEventStateIsNotPublished(Event event) {
        State state = event.getState();

        if (state.equals(State.PUBLISHED)) {
            throw new EventValidationException(String.format("Обновление невозможно - событие уже опубликовано: eventId= %d", event.getId()));
        }
    }

    private void validateEventStateIsNotCancelled(Event event) {
        State state = event.getState();

        if (state.equals(State.CANCELED)) {
            throw new EventValidationException(String.format("Обновление невозможно - событие уже завершено: eventId= %d", event.getId()));
        }
    }

    private void updateEventBasicData(Event event, UpdateEventRequest request) {
        updateTitle(event, request);
        updateAnnotation(event, request);
        updateDescription(event, request);
        updateEventDate(event, request);
        updatePaid(event, request);
        updateRequestModeration(event, request);
        updateParticipantLimit(event, request);
        updateLocation(event, request);
        updateCategory(event, request);
    }

    private void updateTitle(Event event, UpdateEventRequest request) {
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private void updateAnnotation(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
    }

    private void updateDescription(Event event, UpdateEventRequest request) {
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
    }

    private void updateEventDate(Event event, UpdateEventRequest request) {
        if (request.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(request.getEventDate(), FORMATTER));
        }
    }

    private void updatePaid(Event event, UpdateEventRequest request) {
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
    }

    private void updateRequestModeration(Event event, UpdateEventRequest request) {
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
    }

    private void updateParticipantLimit(Event event, UpdateEventRequest request) {
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
    }

    private void updateLocation(Event event, UpdateEventRequest request) {
        if (request.getLocation() != null) {
            Location deletedLocation = event.getLocation();
            locationRepository.delete(deletedLocation);

            Location location = LocationMapper.toLocation(request.getLocation());
            Location newLocation = locationRepository.save(location);

            event.setLocation(newLocation);
        }
    }

    private void updateCategory(Event event, UpdateEventRequest request) {
        if (request.getCategory() != null) {
            Category category = categoryService.getById(request.getCategory());
            event.setCategory(category);
        }
    }
}