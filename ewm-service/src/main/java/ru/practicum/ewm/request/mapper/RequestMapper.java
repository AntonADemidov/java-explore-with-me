package ru.practicum.ewm.request.mapper;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestDto;

import java.time.format.DateTimeFormatter;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RequestMapper {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static RequestDto toRequestDto(Request request) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(request.getId());
        requestDto.setEvent(request.getEvent().getId());
        requestDto.setRequester(request.getRequester().getId());
        requestDto.setStatus(request.getStatus());
        requestDto.setCreated(request.getCreated().format(FORMATTER));
        return requestDto;
    }
}
