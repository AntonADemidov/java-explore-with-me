package ru.practicum.ewm.compilation.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.model.EventShortDto;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    Long id;
    String title;
    Boolean pinned;
    List<EventShortDto> events;
}
