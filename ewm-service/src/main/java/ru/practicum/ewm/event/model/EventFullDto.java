package ru.practicum.ewm.event.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.model.CategoryDto;
import ru.practicum.ewm.user.model.UserShortDto;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;
    String title;
    String annotation;
    String description;
    State state;
    String eventDate;
    String createdOn;
    String publishedOn;
    Boolean paid;
    Boolean requestModeration;
    Long participantLimit;
    LocationDto location;
    CategoryDto category;
    UserShortDto initiator;
    Long confirmedRequests;

    //TODO
    /*
    Long views;
     */
}