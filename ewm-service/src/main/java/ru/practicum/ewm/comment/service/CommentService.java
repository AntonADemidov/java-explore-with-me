package ru.practicum.ewm.comment.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.model.CommentDto;
import ru.practicum.ewm.comment.model.CommentStatusUpdateRequest;
import ru.practicum.ewm.comment.model.CommentStatusUpdateResult;
import ru.practicum.ewm.comment.model.NewCommentDto;

import java.util.List;

@Transactional(readOnly = true)
public interface CommentService {
    @Transactional
    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getListOfCommentsByAuthor(Long userId);

    @Transactional
    CommentDto deleteComment(Long userId, Long commentId);

    List<CommentDto> getCommentsByEventOwner(Long userId, Long eventId);

    @Transactional
    CommentStatusUpdateResult updateCommentsStatusByEventOwner(Long userId, Long eventId,
                                                               CommentStatusUpdateRequest request, String text);

    CommentDto getCommentByAuthor(Long userId, Long commentId);
}