package ru.practicum.ewm.comment.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.model.*;

import java.util.List;

@Transactional(readOnly = true)
public interface CommentService {
    @Transactional
    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getListOfCommentsByAuthor(Long userId);

    @Transactional
    CommentDto deleteCommentByAuthor(Long userId, Long commentId);

    List<CommentDto> getListOfCommentsByEventOwner(Long userId, Long eventId);

    @Transactional
    CommentStatusUpdateResult updateCommentsStatusByEventOwner(Long userId, Long eventId,
                                                               CommentStatusUpdateRequest request, String text);

    CommentDto getCommentByAuthor(Long userId, Long commentId);

    CommentDto getCommentByEventOwner(Long userId, Long eventId, Long commentId);

    @Transactional
    CommentDto deleteCommentByEventOwner(Long userId, Long eventId, Long commentId);

    @Transactional
    CommentDto updateCommentByAuthor(Long userId, Long commentId, UpdateCommentRequest request, String text);

    List<CommentPublicDto> getPublicComments(Long eventId, Integer from, Integer size);

    CommentPublicDto getPublicCommentById(Long eventId, Long commentId);
}