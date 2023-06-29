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
import ru.practicum.ewm.comment.model.NewCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentController {
    CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive Long userId,
                                    @RequestParam @Positive Long eventId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Создание нового комментария {} пользователем с userId={} к событию с eventId={}.", newCommentDto, userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }
}