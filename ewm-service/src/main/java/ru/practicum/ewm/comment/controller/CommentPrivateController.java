package ru.practicum.ewm.comment.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.model.*;
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
    CommentService commentService;
    static final String COMMENTS = "/comments";
    static final String ID = "/{commentId}";
    static final String DELETE = "/delete";
    static final String EVENTS_ID_COMMENTS = "/events/{eventId}/comments";

    @PostMapping(COMMENTS)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive Long userId,
                                    @RequestParam @Positive Long eventId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Создание нового комментария {} пользователем с userId={} к событию с eventId={}.", newCommentDto, userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @GetMapping(COMMENTS)
    public List<CommentDto> getListOfCommentsByAuthor(@PathVariable @Positive Long userId) {
        log.info("Просмотр списка своих комментариев автором: userId={}", userId);
        return commentService.getListOfCommentsByAuthor(userId);
    }

    @GetMapping(COMMENTS + ID)
    public CommentDto getCommentByAuthor(@PathVariable @Positive Long userId,
                                         @PathVariable @Positive Long commentId) {
        log.info("Просмотр комментария автором события: userId={}, commentId={}", userId, commentId);
        return commentService.getCommentByAuthor(userId, commentId);
    }

    @PatchMapping(COMMENTS + ID)
    public CommentDto updateComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId,
                                    @RequestBody @Valid UpdateCommentRequest request,
                                    @RequestParam(value = "text", required = false) String text) {
        log.info("Обновление комментария автором: authorId={}, commentId={}", userId, commentId);
        return commentService.updateCommentByAuthor(userId, commentId, request, text);
    }


    @PatchMapping(COMMENTS + ID + DELETE)
    public CommentDto deleteComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId) {
        log.info("Удаление комментария автором: authorId={}, commentId={}", userId, commentId);
        return commentService.deleteCommentByAuthor(userId, commentId);
    }

    @GetMapping(EVENTS_ID_COMMENTS)
    public List<CommentDto> getListOfCommentsByEventOwner(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId) {
        log.info("Просмотр комментариев инициатором события: eventId={}, initiatorId={}.", eventId, userId);
        return commentService.getListOfCommentsByEventOwner(userId, eventId);
    }

    @PatchMapping(EVENTS_ID_COMMENTS)
    public CommentStatusUpdateResult updateCommentsStatusByEventOwner(@PathVariable @Positive Long userId,
                                                                      @PathVariable @Positive Long eventId,
                                                                      @RequestBody @Valid CommentStatusUpdateRequest request,
                                                                      @RequestParam(value = "text", required = false) String text) {
        log.info("Обновление статусов комментариев инициатором события: eventId={}, initiatorId={}.", eventId, userId);
        return commentService.updateCommentsStatusByEventOwner(userId, eventId, request, text);
    }

    @GetMapping(EVENTS_ID_COMMENTS + ID)
    public CommentDto getCommentByEventOwner(@PathVariable @Positive Long userId,
                                             @PathVariable @Positive Long eventId,
                                             @PathVariable @Positive Long commentId) {
        log.info("Просмотр комментария инициатором события: initiatorId={}, eventId={}, commentId={}", userId, eventId, commentId);
        return commentService.getCommentByEventOwner(userId, eventId, commentId);
    }

    @PatchMapping(EVENTS_ID_COMMENTS + ID + DELETE)
    public CommentDto deleteCommentByEventOwner(@PathVariable @Positive Long userId,
                                                @PathVariable @Positive Long eventId,
                                                @PathVariable @Positive Long commentId) {
        log.info("Удаление комментария инициатором события: initiatorId={}, eventId={}, commentId={}", userId, eventId, commentId);
        return commentService.deleteCommentByEventOwner(userId, eventId, commentId);
    }
}