package ru.practicum.ewm.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentDto;
import ru.practicum.ewm.comment.model.NewCommentDto;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.util.exception.comment.CommentValidationException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentServiceImpl implements CommentService {
    CommentRepository commentRepository;
    UserService userService;
    EventService eventService;
    RequestRepository requestRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        setModerationStatus(newCommentDto);
        setClosedComments(newCommentDto);
        validateClosedComments(newCommentDto, user, event);

        Comment comment = CommentMapper.toComment(newCommentDto, user, event);
        Comment actualComment = commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.toCommentDto(actualComment);
        log.info("Новый комментарий добавлен в базу: commentId={}.", commentDto.getId());
        return commentDto;
    }

    private void setClosedComments(NewCommentDto newCommentDto) {
        if (newCommentDto.getClosedComments() == null) {
            newCommentDto.setClosedComments(true);
        }
    }

    private void setModerationStatus(NewCommentDto newCommentDto) {
        if (newCommentDto.getCommentModeration() == null) {
            newCommentDto.setCommentModeration(true);
        }
    }

    private void validateClosedComments(NewCommentDto newCommentDto, User user, Event event) {
        if (newCommentDto.getClosedComments()) {
            validateEventParticipation(user, event);
        }
    }

    private void validateEventParticipation(User user, Event event) {
        Request request = requestRepository.findByRequesterEqualsAndEventEquals(user, event).orElseThrow(() ->
                new CommentValidationException(String.format("Пользователь с userId=%d не подавал заявок на участие в событии с eventId=%d.",
                        user.getId(), event.getId())));

        if (!request.getStatus().equals(RequestState.CONFIRMED)) {
            throw new CommentValidationException(String.format("Заявка пользователя с userId=%d на участие в событии с eventId=%d не была одобрена",
                    user.getId(), event.getId()));
        }
    }
}