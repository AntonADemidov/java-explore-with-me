package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentStatus;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuthorEqualsAndStatusNotAndStatusNot(User user, CommentStatus firstStatus, CommentStatus secondStatus);

    List<Comment> findByEventEquals(Event event);

    List<Comment> findByIdIn(List<Long> commentIds);

    Page<Comment> findAllByEventEqualsAndStatusEquals(Event event, CommentStatus status, Pageable request);
}
