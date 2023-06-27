package ru.practicum.ewm.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.DateValidationException;
import ru.practicum.ewm.EndpointHitFromUserDto;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.util.PageNumber;
import ru.practicum.ewm.util.exception.event.EventNotFoundException;
import ru.practicum.ewm.util.exception.event.EventValidationException;

import javax.servlet.http.HttpServletRequest;
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
    EventRepository eventRepository;
    LocationRepository locationRepository;
    UserService userService;
    CategoryService categoryService;
    StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userService.getUserById(userId);

        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }

        Event event = EventMapper.toEvent(newEventDto);
        validateEventDate(event, 1);

        Location location = LocationMapper.toLocation(newEventDto.getLocation());
        Location actualLocation = locationRepository.save(location);
        Category category = categoryService.getById(newEventDto.getCategory());

        event.setState(State.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(LocalDateTime.now());
        event.setLocation(actualLocation);
        event.setCategory(category);
        event.setInitiator(user);

        Event actualEvent = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(actualEvent);
        log.info("Новое событие добавлено в базу: {} c id #{}.", eventFullDto.getTitle(), eventFullDto.getId());
        return eventFullDto;
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
        validateDateForEventUpdating(event, request, 2);
        updateEventBasicData(event, request);
        updateStateByOwner(event, request);

        Event actualEvent = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(actualEvent);
        log.info("Пользователь с userId={} обновил событие в базе: {}, eventId= {}.", userId, eventFullDto.getTitle(), eventFullDto.getId());
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest request) {
        Event event = getEventById(eventId);

        validateDateForEventUpdating(event, request, 1);
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
        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        List<BooleanExpression> conditions = new ArrayList<>();
        Page<Event> requestPage;
        QEvent event = QEvent.event;

        getUsersConditions(users, conditions, event);
        getStatesConditions(states, conditions, event);
        getCategoriesConditions(categories, conditions, event);
        getDateConditions(rangeStart, rangeEnd, conditions, event, false);


        if (conditions.size() != 0) {
            BooleanExpression finalCondition = getFinalCondition(conditions);
            requestPage = eventRepository.findAll(finalCondition, request);
        } else {
            requestPage = eventRepository.findAll(request);
        }

        List<EventFullDto> eventFullDtos = requestPage.getContent().stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

        log.info("Список событий сформирован: количество элементов={}.", eventFullDtos.size());
        return eventFullDtos;
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                               String rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size,
                                               HttpServletRequest httpServletRequest) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String uri = httpServletRequest.getRequestURI();
        String ip = httpServletRequest.getRemoteAddr();

        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        Comparator<EventShortDto> comparator = getComparator(sort);
        List<BooleanExpression> conditions = new ArrayList<>();
        List<EventShortDto> events;
        QEvent event = QEvent.event;

        conditions.add(event.state.eq(State.PUBLISHED));
        getTextCondition(text, conditions, event);
        getCategoriesConditions(categories, conditions, event);
        getPaidCondition(paid, conditions, event);
        getDateConditions(rangeStart, rangeEnd, conditions, event, true);

        BooleanExpression finalCondition = getFinalCondition(conditions);
        Page<Event> requestPage = eventRepository.findAll(finalCondition, request);

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

        saveStats(uri, ip, timestamp);
        log.info("Список событий сформирован: количество элементов={}.", events.size());
        return events;
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest httpServletRequest) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String uri = httpServletRequest.getRequestURI();
        String ip = httpServletRequest.getRemoteAddr();
        Event event = getEventById(eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new EventNotFoundException(String.format("Меропритие с eventId=%d не опубликовано.", event.getId()));
        }

        saveStats(uri, ip, timestamp);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, uri, statsClient);
        log.info("Просмотр события по eventId={}.", eventFullDto.getId());
        return eventFullDto;
    }

    private void saveStats(String uri, String ip, String timestamp) {
        EndpointHitFromUserDto endpointHitFromUserDto = getEndpointHitFromUserDto(uri, ip, timestamp);
        statsClient.createEndpointHit(endpointHitFromUserDto);
        log.info("Статистика обращения к эндпоинту {} сохранена.", endpointHitFromUserDto.getUri());
    }

    private EndpointHitFromUserDto getEndpointHitFromUserDto(String uri, String ip, String timestamp) {
        EndpointHitFromUserDto endpointHitFromUserDto = new EndpointHitFromUserDto();
        String app = "ewm-service";
        endpointHitFromUserDto.setApp(app);
        endpointHitFromUserDto.setUri(uri);
        endpointHitFromUserDto.setIp(ip);
        endpointHitFromUserDto.setTimestamp(timestamp);
        return endpointHitFromUserDto;
    }

    private void validateEventDate(Event event, int interval) {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime eventDate = event.getEventDate();

        if (!current.plusHours(interval).isBefore(eventDate)) {
            throw new DateValidationException(String.format("До начала события должно быть не менее, чем %dч. от текущего момента. Начало события: %s",
                    interval, eventDate.format(FORMATTER)));
        }
    }

    private void validateDateForEventUpdating(Event event, UpdateEventRequest request, int interval) {
        validateEventDate(event, interval);
        LocalDateTime current = LocalDateTime.now();

        if (request.getEventDate() != null) {
            LocalDateTime requestDate = LocalDateTime.parse(request.getEventDate(), FORMATTER);
            if (!current.plusHours(interval).isBefore(requestDate)) {
                throw new DateValidationException(String.format("Новое время события должно быть не ранее, чем через %dч. от текущего момента. Начало события: %s",
                        interval, requestDate.format(FORMATTER)));
            }
        }
    }

    private void getDateConditions(String rangeStart, String rangeEnd, List<BooleanExpression> conditions,
                                   QEvent event, Boolean isNoDiapason) {
        LocalDateTime start;
        LocalDateTime end;

        if ((rangeStart != null) && (rangeEnd != null)) {
            start = LocalDateTime.parse(rangeStart, FORMATTER);
            end = LocalDateTime.parse(rangeEnd, FORMATTER);
            validateDates(start, end);

            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));
            conditions.add((event.eventDate.eq(end)).or(event.eventDate.lt(end)));

        } else if ((rangeStart != null) && (rangeEnd == null)) {
            start = LocalDateTime.parse(rangeStart, FORMATTER);
            conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));

        } else if ((rangeStart == null) && (rangeEnd != null)) {
            end = LocalDateTime.parse(rangeEnd, FORMATTER);
            conditions.add((event.eventDate.eq(end)).or(event.eventDate.lt(end)));

        } else {
            if (isNoDiapason) {
                start = LocalDateTime.now();
                conditions.add((event.eventDate.eq(start)).or(event.eventDate.gt(start)));
            }
        }
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new DateValidationException(String.format("Дата начала диапазона start=%s не может быть позднее даты окончания диапазона end=%s",
                    start, end));
        }
    }

    private void getCategoriesConditions(List<Long> categories, List<BooleanExpression> conditions, QEvent event) {
        if (categories != null) {
            for (Long categoryId : categories) {
                conditions.add(event.category.id.eq(categoryId));
            }
        }
    }

    private void getUsersConditions(List<Long> users, List<BooleanExpression> conditions, QEvent event) {
        if (users != null) {
            for (Long userId : users) {
                conditions.add(event.initiator.id.eq(userId));
            }
        }
    }

    private void getStatesConditions(List<String> states, List<BooleanExpression> conditions, QEvent event) {
        if (states != null) {
            for (String state : states) {
                State eventState = State.valueOf(state);
                conditions.add(event.state.eq(eventState));
            }
        }
    }

    private void getTextCondition(String text, List<BooleanExpression> conditions, QEvent event) {
        if (text != null) {
            String anyText = "%";
            String condition = String.format("%s%s%s", anyText, text, anyText);
            conditions.add((event.annotation.likeIgnoreCase(condition)).or(event.description.likeIgnoreCase(condition)));
        }
    }

    private void getPaidCondition(Boolean paid, List<BooleanExpression> conditions, QEvent event) {
        if (paid != null) {
            conditions.add(event.paid.eq(paid));
        }
    }

    private BooleanExpression getFinalCondition(List<BooleanExpression> conditions) {
        return conditions.stream()
                .reduce(BooleanExpression::and)
                .get();
    }

    private Comparator<EventShortDto> getComparator(String sort) {
        Comparator<EventShortDto> comparator;
        EventSort finalSort = EventSort.valueOf(sort);

        if (finalSort.equals(EventSort.EVENT_DATE)) {
            comparator = Comparator.comparing(EventShortDto::getEventDate);
        } else {
            comparator = Comparator.comparing(EventShortDto::getViews);
        }
        return comparator;
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