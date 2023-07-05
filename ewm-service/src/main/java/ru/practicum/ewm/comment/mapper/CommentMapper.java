package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.model.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Comment toComment(NewCommentDto newCommentDto, User user, Event event) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setStatus(getCommentStatus(event));
        comment.setCreatedOn(LocalDateTime.now());
        setPublishedOn(comment);
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthor(UserMapper.toUserShortDto(comment.getAuthor()));
        commentDto.setEventId(comment.getEvent().getId());
        commentDto.setStatus(comment.getStatus());
        commentDto.setCreatedOn(comment.getCreatedOn().format(FORMATTER));
        setPublishedOn(comment);
        setMessages(commentDto, comment);
        return commentDto;
    }

    private static void setMessages(CommentDto commentDto, Comment comment) {
        if (comment.getMessages() != null) {
            List<MessageDto> messageDtos = comment.getMessages().stream()
                    .map(MessageMapper::toMessageDto)
                    .collect(Collectors.toList());

            commentDto.setMessages(messageDtos);
        }
    }

    private static CommentStatus getCommentStatus(Event event) {
        if (event.getCommentModeration()) {
            return CommentStatus.PENDING;
        } else {
            return CommentStatus.PUBLISHED;
        }
    }

    private static void setPublishedOn(Comment comment) {
        if (comment.getStatus().equals(CommentStatus.PUBLISHED)) {
            comment.setPublishedOn(LocalDateTime.now());
        }
    }

    public static CommentPublicDto toCommentPublicDto(Comment comment) {
        CommentPublicDto commentPublicDto = new CommentPublicDto();
        commentPublicDto.setId(comment.getId());
        commentPublicDto.setAuthorId(comment.getAuthor().getId());
        commentPublicDto.setPublishedOn(comment.getPublishedOn().format(FORMATTER));
        commentPublicDto.setText(comment.getText());
        return commentPublicDto;
    }
}