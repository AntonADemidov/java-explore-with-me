package ru.practicum.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.model.CommentPublicDto;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.event.model.EventFullDto;
import ru.practicum.ewm.event.model.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/events/{eventId}/comments")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentPublicController {
    CommentService commentService;
    static final String ID = "/{commentId}";

    @GetMapping()
    public List<CommentPublicDto> getPublicComments(@PathVariable @Positive Long eventId,
                                              @RequestParam(value = "from", required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(value = "size", required = false, defaultValue = "10") @PositiveOrZero Integer size,
                                              HttpServletRequest request) {
        log.info("Получение списка комментариев по параметрам: eventId={}, from={}, size={}.", eventId, from, size);
        log.info("IP-адрес пользователя: ip={}.", request.getRemoteAddr());
        log.info("Эндпойнт: endpoint={}.", request.getRequestURI());
        return commentService.getPublicComments(eventId, from, size, request);
    }

    @GetMapping(ID)
    public CommentPublicDto getPublicCommentById(@PathVariable @Positive Long eventId,
                                                 @PathVariable @Positive Long commentId,
                                                 HttpServletRequest request) {
        log.info("Получение комментария: commentId={}, eventId={}.", commentId, eventId);
        log.info("IP-адрес пользователя: ip={}.", request.getRemoteAddr());
        log.info("Эндпойнт: endpoint={}.", request.getRequestURI());
        return commentService.getPublicCommentById(eventId, commentId, request);
    }
}