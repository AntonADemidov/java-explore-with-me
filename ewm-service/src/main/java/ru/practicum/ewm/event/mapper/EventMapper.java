package ru.practicum.ewm.event.mapper;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventFullDto;
import ru.practicum.ewm.event.model.EventShortDto;
import ru.practicum.ewm.event.model.NewEventDto;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventMapper {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEvent(NewEventDto newEventDto) {
        Event event = new Event();
        event.setTitle(newEventDto.getTitle());
        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(LocalDateTime.parse(newEventDto.getEventDate(), FORMATTER));
        event.setPaid(newEventDto.getPaid());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        return event;
    }

    public static EventFullDto toEventFullDto(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setState(event.getState());
        eventFullDto.setEventDate(event.getEventDate().format(FORMATTER));
        eventFullDto.setCreatedOn(event.getCreatedOn().format(FORMATTER));
        eventFullDto.setPublishedOn(event.getPublishedOn().format(FORMATTER));
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setLocation(ru.practicum.ewm.event.mapper.LocationMapper.toLocationDto(event.getLocation()));
        eventFullDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventFullDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        eventFullDto.setConfirmedRequests(getConfirmedRequests(event));
        return eventFullDto;
    }

    public static EventShortDto toEventShortDto(Event event) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setEventDate(event.getEventDate().format(FORMATTER));
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventShortDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        eventShortDto.setConfirmedRequests(getConfirmedRequests(event));
        return eventShortDto;
    }

    private static Long getConfirmedRequests(Event event) {
        long confirmedRequests = 0;

        if ((event.getParticipationRequests() != null) && (!event.getParticipationRequests().isEmpty())) {
            confirmedRequests = event.getParticipationRequests().stream()
                    .filter(r -> r.getStatus().equals(RequestState.CONFIRMED))
                    .count();
        }
        return confirmedRequests;
    }
}