package ru.practicum.ewm.request.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestStateUpdateRequest {
    @NotEmpty
    List<Long> requestIds;
    @NotBlank
    String status;
}