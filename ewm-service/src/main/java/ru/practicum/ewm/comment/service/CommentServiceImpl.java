package ru.practicum.ewm.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.*;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.util.exception.comment.CommentNotFoundException;
import ru.practicum.ewm.util.exception.comment.CommentValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        validateEventState(event);
        validateClosedComments(user, event, true);

        Comment comment = CommentMapper.toComment(newCommentDto, user, event);
        Comment actualComment = commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.toCommentDto(actualComment);
        log.info("Новый комментарий добавлен в базу: commentId={}.", commentDto.getId());
        return commentDto;
    }

    @Override
    public List<CommentDto> getCommentsByAuthor(Long userId) {
        User user = userService.getUserById(userId);
        List<Comment> comments = commentRepository.findByAuthorEquals(user);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto deleteComment(Long userId, Long commentId) {
        User user = userService.getUserById(userId);
        Comment comment = getCommentById(commentId);

        validateCommentAuthor(comment, user);

        comment.setStatus(CommentStatus.DELETED);

        Comment actualComment = commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.toCommentDto(actualComment);
        log.info("Комментарий удалён: commentId={}, eventId={}, authorId={}.",
                commentDto.getId(), commentDto.getEvent().getId(), commentDto.getAuthor().getId());
        return commentDto;
    }

    @Override
    public List<CommentDto> getCommentsByEventOwner(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        validateInitiator(user, event);

        List<CommentDto> commentDtos = commentRepository.findByEventEquals(event).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        log.info("Список комментариев сформирован: количество элементов={}.", commentDtos.size());
        return commentDtos;
    }

    private void validateInitiator(User user, Event event) {
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new CommentValidationException(String.format("Событие с eventId=%d не относится к пользователю с userId=%d",
                    event.getId(), user.getId()));
        }
    }

    @Override
    @Transactional
    public CommentStatusUpdateResult updateCommentsStatusByEventOwner(Long userId, Long eventId, CommentStatusUpdateRequest request) {
        List<Comment> actualList = new ArrayList<>();
        List<Comment> publishedComments = new ArrayList<>();
        List<Comment> rejectedComments = new ArrayList<>();
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        validateInitiator(user, event);
        validateClosedComments(user, event, false);

        List<Comment> basicList = commentRepository.findByIdIn(request.getCommentIds());
        updateBasicListOfComments(basicList, actualList, event);

        UpdateCommentStatus status = UpdateCommentStatus.valueOf(request.getStatus());
        updatePublishedAndRejectedLists(actualList, publishedComments, rejectedComments, status);

        CommentStatusUpdateResult result = new CommentStatusUpdateResult();
        result.setPublishedComments(publishedComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        result.setRejectedComments(rejectedComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        return result;
    }

    private void updateBasicListOfComments(List<Comment> basicList, List<Comment> actualList, Event event) {
        for (Comment comment : basicList) {
            if (Objects.equals(comment.getEvent().getId(), event.getId())) {
                if (comment.getStatus().equals(CommentStatus.PENDING)) {
                    actualList.add(comment);
                } else {
                    throw new CommentValidationException(String.format("Обновление невозможно - статус комментария %s не соответствует требуемому: PENDING",
                            comment.getStatus()));
                }
            } else {
                throw new CommentValidationException(String.format("Комментарий c commentId=%d не относится к событию с eventId=%d",
                        comment.getId(), event.getId()));
            }
        }
    }

    private void updatePublishedAndRejectedLists(List<Comment> actualList, List<Comment> publishedComments,
                                                 List<Comment> rejectedComments, UpdateCommentStatus status) {
        for (Comment comment : actualList) {
            if (status.equals(UpdateCommentStatus.REJECTED)) {
                comment.setStatus(CommentStatus.CANCELED);
                rejectedComments.add(commentRepository.save(comment));
            } else {
                comment.setStatus(CommentStatus.PUBLISHED);
                publishedComments.add(commentRepository.save(comment));
            }
        }
    }

    private Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new CommentNotFoundException(String.format("Комментарий с commentId=%d отсутствует в базе.", id)));
    }

    private void validateEventState(Event event) {
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new CommentValidationException(String.format("Нельзя комментировать неопубликованное событие: eventState= %s",
                    event.getState()));
        }
    }

    private void validateCommentAuthor(Comment comment, User user) {
        if (!comment.getAuthor().equals(user)) {
            throw new CommentValidationException(String.format("Невозможно удалить чужой комментарий: userId= %d, authorId= %d",
                    user.getId(), comment.getAuthor().getId()));
        }
    }

    private void validateClosedComments(User user, Event event, Boolean isCreation) {
        if (isCreation) {
            if (event.getClosedComments()) {
                validateEventParticipation(user, event);
            }
        } else {
            if (!event.getClosedComments()) {
                throw new CommentValidationException(String.format("Модерация не требуется: комментарии к событию с eventId=%d публикуются без одобрения",
                        event.getId()));
            }
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