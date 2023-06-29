package ru.practicum.ewm.comment.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.model.CommentDto;
import ru.practicum.ewm.comment.model.NewCommentDto;

@Transactional(readOnly = true)
public interface CommentService {
    @Transactional
    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);
}