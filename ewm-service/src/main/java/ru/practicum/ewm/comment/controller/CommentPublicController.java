package ru.practicum.ewm.comment.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.model.CommentPublicDto;
import ru.practicum.ewm.comment.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/events/{eventId}/comments")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentPublicController {
    CommentService commentService;
    static final String ID = "/{commentId}";

    @GetMapping()
    public List<CommentPublicDto> getPublicComments(@PathVariable @Positive Long eventId,
                                                    @RequestParam(value = "from", required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "10") @PositiveOrZero Integer size) {
        log.info("Получение списка комментариев по параметрам: eventId={}, from={}, size={}.", eventId, from, size);
        return commentService.getPublicComments(eventId, from, size);
    }

    @GetMapping(ID)
    public CommentPublicDto getPublicCommentById(@PathVariable @Positive Long eventId,
                                                 @PathVariable @Positive Long commentId) {
        log.info("Получение комментария: commentId={}, eventId={}.", commentId, eventId);
        return commentService.getPublicCommentById(eventId, commentId);
    }
}