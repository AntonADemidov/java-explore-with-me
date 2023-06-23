package ru.practicum.ewm.request.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.request.model.RequestDto;
import ru.practicum.ewm.request.model.RequestStateUpdateRequest;
import ru.practicum.ewm.request.model.RequestStateUpdateResult;

import java.util.List;

@Transactional
public interface RequestService {
    RequestDto createRequest(Long userId, Long eventId);

    @Transactional(readOnly = true)
    List<RequestDto> getRequestsOfUser(Long userId);

    RequestDto cancelRequest(Long userId, Long requestId);

    RequestStateUpdateResult updateRequestsStatusByEventOwner(Long userId, Long eventId, RequestStateUpdateRequest request);

    @Transactional(readOnly = true)
    List<RequestDto> getRequestByEventOwner(Long userId, Long eventId);
}