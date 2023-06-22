package ru.practicum.ewm.event.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    @Length(min = 3, max = 120)
    String title;

    @Length(min = 20, max = 2000)
    String annotation;

    @Length(min = 20, max = 7000)
    String description;

    String eventDate;

    Boolean paid;

    Boolean requestModeration;

    Long participantLimit;

    LocationDto location;

    Long category;

    String stateAction;
}