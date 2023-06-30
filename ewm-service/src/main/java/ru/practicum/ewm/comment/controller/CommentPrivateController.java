package ru.practicum.ewm.comment.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.model.CommentDto;
import ru.practicum.ewm.comment.model.CommentStatusUpdateRequest;
import ru.practicum.ewm.comment.model.CommentStatusUpdateResult;
import ru.practicum.ewm.comment.model.NewCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentPrivateController {
    static final String COMMENTS = "/comments";
    static final String EVENTS_ID_COMMENTS = "/events/{eventId}/comments";
    static final String ID_DELETE = "/{commentId}/delete";
    CommentService commentService;

    @PostMapping(COMMENTS)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive Long userId,
                                    @RequestParam @Positive Long eventId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Создание нового комментария {} пользователем с userId={} к событию с eventId={}.", newCommentDto, userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @GetMapping(COMMENTS)
    public List<CommentDto> getCommentsByAuthor(@PathVariable @Positive Long userId) {
        log.info("Просмотр комментариев пользователя: userId={}", userId);
        return commentService.getCommentsByAuthor(userId);
    }

    @PatchMapping(COMMENTS + ID_DELETE)
    public CommentDto deleteComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId) {
        log.info("Удаление комментария пользователем: userId={}, requestId={}", userId, commentId);
        return commentService.deleteComment(userId, commentId);
    }

    @GetMapping(EVENTS_ID_COMMENTS)
    public List<CommentDto> getCommentsByEventOwner(@PathVariable @Positive Long userId,
                                                    @PathVariable @Positive Long eventId) {
        log.info("Просмотр комментариев инициатором события: eventId={}, initiatorId={}.", eventId, userId);
        return commentService.getCommentsByEventOwner(userId, eventId);
    }

    @PatchMapping(EVENTS_ID_COMMENTS)
    public CommentStatusUpdateResult updateCommentsStatus(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId,
                                                          @RequestBody @Valid CommentStatusUpdateRequest request) {
        log.info("Обновление статусов комментариев инициатором события: eventId={}, initiatorId={}.", eventId, userId);
        return commentService.updateCommentsStatusByEventOwner(userId, eventId, request);
    }
}