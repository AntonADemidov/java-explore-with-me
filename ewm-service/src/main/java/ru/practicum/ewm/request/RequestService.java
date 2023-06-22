package ru.practicum.ewm.request;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface RequestService {
    RequestDto createRequest(Long userId, Long eventId);

    @Transactional(readOnly = true)
    List<RequestDto> getRequests(Long userId);

    RequestDto updateRequest(Long userId, Long requestId);
}