package ru.practicum.ewm.event.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.model.CategoryDto;
import ru.practicum.ewm.user.model.UserShortDto;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    Long id;
    String title;
    String annotation;
    String eventDate;
    Boolean paid;
    CategoryDto category;
    UserShortDto initiator;
    Long confirmedRequests;
    Integer views;
    Long publishedComments;
}