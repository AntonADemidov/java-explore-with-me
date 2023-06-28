package ru.practicum.ewm.request.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.model.RequestDto;
import ru.practicum.ewm.request.model.RequestStateUpdateRequest;
import ru.practicum.ewm.request.model.RequestStateUpdateResult;
import ru.practicum.ewm.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RequestController {
    static final String REQUESTS = "/requests";
    static final String EVENTS_ID_REQUESTS = "/events/{eventId}/requests";
    static final String REQUEST_ID_CANCEL = "/{requestId}/cancel";
    RequestService requestService;

    @PostMapping(REQUESTS)
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(@PathVariable @Positive Long userId,
                                    @RequestParam(value = "eventId") @Positive Long eventId) {
        log.info("Создание нового запроса: userId={}, eventId={}.", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @GetMapping(REQUESTS)
    public List<RequestDto> getRequestsOfUser(@PathVariable @Positive Long userId) {
        log.info("Просмотр запросов пользователя: userId={}", userId);
        return requestService.getRequestsOfUser(userId);
    }

    @PatchMapping(REQUESTS + REQUEST_ID_CANCEL)
    public RequestDto cancelRequest(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long requestId) {
        log.info("Отмена запроса пользователя: userId={}, requestId={}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @PatchMapping(EVENTS_ID_REQUESTS)
    public RequestStateUpdateResult updateRequestsStatus(@PathVariable @Positive Long userId,
                                                         @PathVariable @Positive Long eventId,
                                                         @RequestBody @Valid RequestStateUpdateRequest request) {
        log.info("Обновление статусов запросов инициатором события: eventId={}, InitiatorId={}.", eventId, userId);
        return requestService.updateRequestsStatusByEventOwner(userId, eventId, request);
    }

    @GetMapping(EVENTS_ID_REQUESTS)
    public List<RequestDto> getRequestByEventOwner(@PathVariable @Positive Long userId,
                                                   @PathVariable @Positive Long eventId) {
        log.info("Просмотр запросов инициатором события: eventId={}, InitiatorId={}.", eventId, userId);
        return requestService.getRequestByEventOwner(userId, eventId);
    }
}