package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByRequesterEqualsAndEventEquals(User requester, Event event);

    List<Request> findByRequesterEquals(User requester);

    List<Request> findByIdIn(List<Long> list);

    List<Request> findByEventEquals(Event event);
}