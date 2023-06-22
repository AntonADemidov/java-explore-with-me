package ru.practicum.ewm.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.UserService;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RequestServiceImpl implements RequestService {
    RequestRepository requestRepository;
    UserService userService;
    EventService eventService;

    //TODO
    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        userService.getUserById(userId);
        return null;
    }

    @Override
    public RequestDto updateRequest(Long userId, Long requestId) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequests(Long userId) {
        return null;
    }
}