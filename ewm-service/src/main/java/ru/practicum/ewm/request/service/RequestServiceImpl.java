package ru.practicum.ewm.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.*;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.exception.RequestNotFoundException;
import ru.practicum.ewm.request.exception.RequestValidationException;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RequestServiceImpl implements RequestService {
    RequestRepository requestRepository;
    UserService userService;
    EventService eventService;

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        validateRequestInBase(user, event);
        validateRequester(user, event);
        validateEventState(event);
        validateParticipationLimit(event);
        RequestState state = getRequestStatus(event);

        Request request = new Request();
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(state);
        request.setCreated(LocalDateTime.now());

        Request actualRequest = requestRepository.save(request);
        RequestDto requestDto = RequestMapper.toRequestDto(actualRequest);
        log.info("Новый запрос добавлен в базу: requestId={}, eventId={}, requesterId={}.",
                requestDto.getId(), requestDto.getEvent(), requestDto.getRequester());
        return requestDto;
    }

    @Override
    public RequestStateUpdateResult updateRequestsStatusByEventOwner(Long userId, Long eventId, RequestStateUpdateRequest updateRequest) {
        List<Request> actualList = new ArrayList<>();
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);
        validateEventState(event);

        List<Request> basicList = requestRepository.findByIdIn(updateRequest.getRequestIds());
        for (Request request : basicList) {
            if (Objects.equals(request.getEvent().getId(), event.getId())) {
                if (request.getStatus().equals(RequestState.PENDING)) {
                    actualList.add(request);
                } else {
                    throw new RequestValidationException(String.format("Обновление невозможно: статус запроса %s не соответствует требуемому PENDING",
                            request.getStatus()));
                }
            } else {
                throw new RequestValidationException(String.format("Запрос c requestId=%d не относится к событию с eventId=%d",
                        request.getId(), event.getId()));
            }
        }

        UpdateRequestState status = UpdateRequestState.valueOf(updateRequest.getStatus());
        for (Request request : actualList) {
            if (status.equals(UpdateRequestState.REJECTED)) {
                request.setStatus(RequestState.REJECTED);
                rejectedRequests.add(requestRepository.save(request));
            } else {
                if (validate(event)) {
                    request.setStatus(RequestState.CONFIRMED);
                    confirmedRequests.add(requestRepository.save(request));
                } else {
                    request.setStatus(RequestState.REJECTED);
                    rejectedRequests.add(requestRepository.save(request));
                    throw new RequestValidationException(String.format("Обработать запрос невозможно - достигнут лимит участников: %d",
                            event.getParticipantLimit()));
                }
            }
        }

        RequestStateUpdateResult result = new RequestStateUpdateResult();
        result.setConfirmedRequests(confirmedRequests.stream().map(RequestMapper::toRequestDto).collect(Collectors.toList()));
        result.setRejectedRequests(rejectedRequests.stream().map(RequestMapper::toRequestDto).collect(Collectors.toList()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequestByEventOwner(Long eventId, Long userId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new RequestValidationException(String.format("Событие с eventId=%d не относится к пользователю с userId=%d",
                    eventId, userId));
        }

        List<Request> requests = requestRepository.findByEventEquals(event);
        List<RequestDto> requestDtos = requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
        log.info("Список запросов сформирован: количество элементов={}.", requestDtos.size());
        return requestDtos;
    }

    private boolean validate(Event event) {
        long confirmedRequests = event.getParticipationRequests().stream()
                .filter(r -> r.getStatus().equals(RequestState.CONFIRMED))
                .count();

        return confirmedRequests < event.getParticipantLimit();
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        User user = userService.getUserById(userId);
        Request request = getRequestById(requestId);

        validateRequestOwner(request, user);
        request.setStatus(RequestState.CANCELED);

        Request actualRequest = requestRepository.save(request);
        RequestDto requestDto = RequestMapper.toRequestDto(actualRequest);
        log.info("Запрос отменен: requestId={}, eventId={}, requesterId={}.",
                requestDto.getId(), requestDto.getEvent(), requestDto.getRequester());
        return requestDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsOfUser(Long userId) {
        User user = userService.getUserById(userId);
        List<Request> requests = requestRepository.findByRequesterEquals(user);

        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    private Request getRequestById(Long id) {
        return requestRepository.findById(id).orElseThrow(() ->
                new RequestNotFoundException(String.format("Запрос с id=%d отсутствует в базе.", id)));
    }

    private void validateRequestInBase(User user, Event event) {
        Optional<Request> optionalRequest = requestRepository.findByRequesterEqualsAndEventEquals(user, event);

        if (optionalRequest.isPresent()) {
            Request request = optionalRequest.get();
            throw new RequestValidationException(String.format("Запрос уже есть в базе: requestId= %d, userId= %d, eventId= %d",
                    request.getId(), request.getRequester().getId(), request.getEvent().getId()));
        }
    }

    private void validateRequester(User user, Event event) {
        if (event.getInitiator().equals(user)) {
            throw new RequestValidationException(String.format("Запрос на участие в собственном событии со стороны инициатора невозможен: userId= %d, initiatorId= %d",
                    user.getId(), event.getInitiator().getId()));
        }
    }

    private void validateEventState(Event event) {
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new RequestValidationException(String.format("Невозможно принять участие в неопубликованном событии: eventState= %s",
                    event.getState()));
        }
    }

    private void validateParticipationLimit(Event event) {
        if (event.getParticipantLimit() != 0) {
            long confirmedRequests = event.getParticipationRequests().stream()
                    .filter(r -> r.getStatus().equals(RequestState.CONFIRMED))
                    .count();

            if (!(confirmedRequests < event.getParticipantLimit())) {
                throw new RequestValidationException(String.format("Обработать запрос невозможно - достигнут лимит участников: %d",
                        event.getParticipantLimit()));
            }
        }
    }

    private RequestState getRequestStatus(Event event) {
        RequestState state;
        if (event.getRequestModeration() || event.getParticipantLimit() == 0) {
            state = RequestState.PENDING;
        } else {
            state = RequestState.CONFIRMED;
        }
        return state;
    }

    private void validateRequestOwner(Request request, User user) {
        if (!request.getRequester().equals(user)) {
            throw new RequestValidationException(String.format("Невозможно отменить чужой запрос: userId= %d, requesterId= %d",
                    user.getId(), request.getRequester().getId()));
        }
    }
}