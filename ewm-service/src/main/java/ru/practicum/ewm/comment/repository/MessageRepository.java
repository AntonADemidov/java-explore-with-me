package ru.practicum.ewm.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comment.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
