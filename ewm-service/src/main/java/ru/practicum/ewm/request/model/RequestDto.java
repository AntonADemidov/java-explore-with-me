package ru.practicum.ewm.request.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestDto {
    Long id;
    Long event;
    Long requester;
    RequestState status;
    String created;
}