package ru.practicum.ewm.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RequestController {
    RequestService requestService;

    @PostMapping("/{userId}/requests")
    public RequestDto createRequest(@PathVariable @Positive Long userId,
                                    @RequestParam(value = "eventId") @Positive Long eventId) {
        log.info("Создание нового запроса: userId={}, eventId={}.", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<RequestDto> getRequests(@PathVariable @Positive Long userId) {
        log.info("Просмотр запросов пользователя: userId={}", userId);
        return requestService.getRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto updateRequest(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long requestId) {
        log.info("Отмена запроса пользователя: userId={}, requestId={}", userId, requestId);
        return requestService.updateRequest(userId, requestId);
    }
}