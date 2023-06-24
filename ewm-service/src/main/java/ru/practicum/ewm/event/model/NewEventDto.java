package ru.practicum.ewm.event.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank
    @Length(min = 3, max = 120)
    String title;

    @NotBlank
    @Length(min = 20, max = 2000)
    String annotation;

    @NotBlank
    @Length(min = 20, max = 7000)
    String description;

    @NotBlank
    String eventDate;

    @Value("false")
    Boolean paid;

    @Value("true")
    Boolean requestModeration;

    @Value("0")
    Long participantLimit;

    @NotNull
    ru.practicum.ewm.event.model.LocationDto location;

    @NotNull
    Long category;
}