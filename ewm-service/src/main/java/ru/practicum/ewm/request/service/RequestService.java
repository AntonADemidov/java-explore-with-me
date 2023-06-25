package ru.practicum.ewm.request.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.request.model.RequestDto;
import ru.practicum.ewm.request.model.RequestStateUpdateRequest;
import ru.practicum.ewm.request.model.RequestStateUpdateResult;

import java.util.List;

@Transactional(readOnly = true)
public interface RequestService {
    @Transactional
    RequestDto createRequest(Long userId, Long eventId);

    List<RequestDto> getRequestsOfUser(Long userId);

    @Transactional
    RequestDto cancelRequest(Long userId, Long requestId);

    @Transactional
    RequestStateUpdateResult updateRequestsStatusByEventOwner(Long userId, Long eventId, RequestStateUpdateRequest request);

    List<RequestDto> getRequestByEventOwner(Long userId, Long eventId);
}