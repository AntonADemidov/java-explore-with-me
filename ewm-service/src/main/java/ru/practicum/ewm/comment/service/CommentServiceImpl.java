package ru.practicum.ewm.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.*;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.comment.repository.MessageRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.util.PageNumber;
import ru.practicum.ewm.util.exception.comment.CommentNotFoundException;
import ru.practicum.ewm.util.exception.comment.CommentValidationException;

import java.time.LocalDateTime;
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
    MessageRepository messageRepository;

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
    public CommentDto getCommentByAuthor(Long userId, Long commentId) {
        return getCommentBasicMethod(userId, null, commentId, true);
    }

    @Override
    public CommentDto getCommentByEventOwner(Long userId, Long eventId, Long commentId) {
        return getCommentBasicMethod(userId, eventId, commentId, false);
    }

    @Override
    public CommentPublicDto getPublicCommentById(Long eventId, Long commentId) {
        eventService.getEventById(eventId);
        Comment comment = getCommentById(commentId);

        if (!comment.getStatus().equals(CommentStatus.PUBLISHED)) {
            throw new CommentValidationException(String.format("Комментарий с commentId=%d не опубликован.", comment.getId()));
        }

        CommentPublicDto commentPublicDto = CommentMapper.toCommentPublicDto(comment);
        log.info("Просмотр комментария: commentId={}.", commentPublicDto.getId());
        return commentPublicDto;
    }

    @Override
    public List<CommentDto> getListOfCommentsByAuthor(Long userId) {
        User user = userService.getUserById(userId);
        List<Comment> comments = commentRepository.findByAuthorEqualsAndStatusNotAndStatusNot(user, CommentStatus.DELETED, CommentStatus.CANCELED);

        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        log.info("Список комментариев автора сформирован: количество элементов={}.", commentDtos.size());
        return commentDtos;
    }

    @Override
    public List<CommentDto> getListOfCommentsByEventOwner(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        validateEventInitiator(user, event);

        List<CommentDto> commentDtos = commentRepository.findByEventEquals(event).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        log.info("Список комментариев сформирован: количество элементов={}.", commentDtos.size());
        return commentDtos;
    }

    @Override
    public List<CommentPublicDto> getPublicComments(Long eventId, Integer from, Integer size) {
        Event event = eventService.getEventById(eventId);

        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        Page<Comment> requestPage = commentRepository.findAllByEventEqualsAndStatusEquals(event, CommentStatus.PUBLISHED, request);

        List<CommentPublicDto> commentPublicDtos = requestPage.stream()
                .map(CommentMapper::toCommentPublicDto)
                .collect(Collectors.toList());

        log.info("Список комментариев сформирован: количество элементов={}.", commentPublicDtos.size());
        return commentPublicDtos;
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAuthor(Long userId, Long commentId, UpdateCommentRequest request, String text) {
        User user = userService.getUserById(userId);
        Comment comment = getCommentById(commentId);

        validateCommentAuthor(comment, user);
        checkCommentStatus(comment, true, true);

        comment.setText(request.getText());

        if (comment.getStatus().equals(CommentStatus.REVIEWED)) {
            if (text != null) {
                Message message = createMessage(text, comment);
                Message actualMessage = messageRepository.save(message);
                log.info("Создано сообщение для инициатора события: messageId={}.", actualMessage.getId());
            }
            comment.setStatus(CommentStatus.PENDING);
        }

        Comment actualComment = commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.toCommentDto(actualComment);

        log.info("Комментарий обновлён автором: commentId={}, eventId={}, authorId={}.",
                commentDto.getId(), commentDto.getEventId(), commentDto.getAuthor().getId());
        return commentDto;
    }

    @Override
    @Transactional
    public CommentStatusUpdateResult updateCommentsStatusByEventOwner(Long userId, Long eventId,
                                                                      CommentStatusUpdateRequest request, String text) {
        List<Comment> actualList = new ArrayList<>();
        List<Comment> publishedComments = new ArrayList<>();
        List<Comment> reviewedComments = new ArrayList<>();
        List<Comment> rejectedComments = new ArrayList<>();
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        validateEventInitiator(user, event);
        validateClosedComments(user, event, false);

        List<Comment> basicList = commentRepository.findByIdIn(request.getCommentIds());
        updateBasicListOfComments(basicList, actualList, event);

        UpdateCommentStatus status = UpdateCommentStatus.valueOf(request.getStatus());
        updateResultLists(actualList, publishedComments, reviewedComments, rejectedComments, status, text);

        CommentStatusUpdateResult result = new CommentStatusUpdateResult();
        result.setPublishedComments(publishedComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        result.setReviewedComments(reviewedComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        result.setRejectedComments(rejectedComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));

        log.info("Статус комментариев обновлен администротором: опубликовано={}, возвращено на коррекцию={}, отклонено={}",
                publishedComments.size(), reviewedComments.size(), rejectedComments.size());
        return result;
    }

    @Override
    @Transactional
    public CommentDto deleteCommentByAuthor(Long userId, Long commentId) {
        return deleteCommentBasicMethod(userId, null, commentId);
    }

    @Override
    @Transactional
    public CommentDto deleteCommentByEventOwner(Long userId, Long eventId, Long commentId) {
        return deleteCommentBasicMethod(userId, eventId, commentId);
    }

    private CommentDto getCommentBasicMethod(Long userId, @Nullable Long eventId, Long commentId, Boolean isAuthor) {
        User user = userService.getUserById(userId);

        if (eventId != null) {
            Event event = eventService.getEventById(eventId);
            validateEventInitiator(user, event);
        }

        Comment comment = getCommentById(commentId);
        checkCommentStatus(comment, isAuthor, false);
        validateCommentAuthorOrEventInitiator(comment, user);

        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        log.info("Комментарий найден: authorId={}, commentId={}", commentDto.getId(), user.getId());
        return commentDto;
    }

    private Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new CommentNotFoundException(String.format("Комментарий с commentId=%d отсутствует в базе.", id)));
    }

    private CommentDto deleteCommentBasicMethod(Long userId, @Nullable Long eventId, Long commentId) {
        User user = userService.getUserById(userId);

        if (eventId != null) {
            Event event = eventService.getEventById(eventId);
            validateEventInitiator(user, event);
        }

        Comment comment = getCommentById(commentId);
        validateCommentAuthorOrEventInitiator(comment, user);
        setNewCommentStatus(comment);

        Comment actualComment = commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.toCommentDto(actualComment);

        log.info("Комментарий удалён: commentId={}, eventId={}, authorId={}.",
                commentDto.getId(), commentDto.getEventId(), commentDto.getAuthor().getId());
        return commentDto;
    }

    private void validateCommentAuthor(Comment comment, User user) {
        if (!comment.getAuthor().equals(user)) {
            throw new CommentValidationException(String.format("Комментарий не относится к данному пользователю: commentId=%d, authorId=%d, userId=%d",
                    comment.getId(), comment.getAuthor().getId(), user.getId()));
        }
    }

    private void setNewCommentStatus(Comment comment) {
        if (comment.getStatus().equals(CommentStatus.PENDING) || comment.getStatus().equals(CommentStatus.REVIEWED)) {
            comment.setStatus(CommentStatus.CANCELED);
        } else if (comment.getStatus().equals(CommentStatus.PUBLISHED)) {
            comment.setStatus(CommentStatus.DELETED);
        } else {
            throw new CommentValidationException(String.format("Комментарий уже был удалён: commentId=%d", comment.getId()));
        }
    }

    private void validateEventInitiator(User user, Event event) {
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new CommentValidationException(String.format("Событие с eventId=%d не относится к пользователю с userId=%d.",
                    event.getId(), user.getId()));
        }
    }

    private void checkCommentStatus(Comment comment, Boolean isAuthor, Boolean isUpdate) {
        if (isAuthor) {
            if (comment.getStatus().equals(CommentStatus.DELETED) || comment.getStatus().equals(CommentStatus.CANCELED)) {
                throw new CommentNotFoundException(String.format("Комментарий с commentId=%d был удалён из базы.",
                        comment.getId()));
            }
        }

        if (isUpdate) {
            if (comment.getStatus().equals(CommentStatus.PUBLISHED)) {
                throw new CommentValidationException(String.format("Обновление невозможно - статус комментария %s не соответствует требуемому: PENDING или REVIEWED.",
                        comment.getStatus()));
            }
        }
    }

    private void updateBasicListOfComments(List<Comment> basicList, List<Comment> actualList, Event event) {
        for (Comment comment : basicList) {
            if (Objects.equals(comment.getEvent().getId(), event.getId())) {
                if (comment.getStatus().equals(CommentStatus.PENDING)) {
                    actualList.add(comment);
                } else {
                    throw new CommentValidationException(String.format("Обновление невозможно - статус комментария %s не соответствует требуемому: PENDING.",
                            comment.getStatus()));
                }
            } else {
                throw new CommentValidationException(String.format("Комментарий c commentId=%d не относится к событию с eventId=%d.",
                        comment.getId(), event.getId()));
            }
        }
    }

    private void updateResultLists(List<Comment> actualList, List<Comment> publishedComments,
                                   List<Comment> reviewedComments, List<Comment> rejectedComments,
                                   UpdateCommentStatus status, String text) {
        for (Comment comment : actualList) {
            if (status.equals(UpdateCommentStatus.CONFIRMED)) {
                comment.setStatus(CommentStatus.PUBLISHED);
                comment.setPublishedOn(LocalDateTime.now());
                Comment actualComment = commentRepository.save(comment);
                publishedComments.add(actualComment);

            } else if (status.equals(UpdateCommentStatus.RETURNED_TO_CORRECTION)) {
                if (text != null) {
                    Message message = createMessage(text, comment);
                    Message actualMessage = messageRepository.save(message);
                    log.info("Создано сообщение для автора комментария: messageId={}.", actualMessage.getId());
                }

                comment.setStatus(CommentStatus.REVIEWED);
                reviewedComments.add(commentRepository.save(comment));

            } else {
                comment.setStatus(CommentStatus.CANCELED);
                rejectedComments.add(commentRepository.save(comment));
            }
        }
    }

    private Message createMessage(String text, Comment comment) {
        Message message = new Message();
        message.setComment(comment);
        message.setText(text);
        message.setSenderId(comment.getEvent().getInitiator().getId());
        message.setCreatedOn(LocalDateTime.now());
        return message;
    }

    private void validateEventState(Event event) {
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new CommentValidationException(String.format("Нельзя комментировать неопубликованное событие: eventState= %s.",
                    event.getState()));
        }
    }

    private void validateCommentAuthorOrEventInitiator(Comment comment, User user) {
        if (!(comment.getAuthor().equals(user) || comment.getEvent().getInitiator().equals(user))) {
            throw new CommentValidationException(String.format("Действие с комментарием могут совершать его автор или" +
                            "инициатор события: authorId= %d / initiatorId=%d. Действие запрошено пользователем с userId=%d",
                    comment.getAuthor().getId(), comment.getEvent().getInitiator().getId(), user.getId()));
        }
    }

    private void validateClosedComments(User user, Event event, Boolean isCreation) {
        if (isCreation) {
            if (event.getClosedComments()) {
                validateEventParticipation(user, event);
            }
        } else {
            if (!event.getClosedComments()) {
                throw new CommentValidationException(String.format("Модерация не требуется: комментарии к событию с eventId=%d публикуются без одобрения.",
                        event.getId()));
            }
        }
    }

    private void validateEventParticipation(User user, Event event) {
        Request request = requestRepository.findByRequesterEqualsAndEventEquals(user, event).orElseThrow(() ->
                new CommentValidationException(String.format("Пользователь с userId=%d не подавал заявок на участие в событии с eventId=%d.",
                        user.getId(), event.getId())));

        if (!request.getStatus().equals(RequestState.CONFIRMED)) {
            throw new CommentValidationException(String.format("Заявка пользователя с userId=%d на участие в событии с eventId=%d не была одобрена.",
                    user.getId(), event.getId()));
        }
    }
}