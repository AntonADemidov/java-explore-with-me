package ru.practicum.ewm.request.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestStateUpdateRequest {
    List<Long> requestIds;
    String status;
}