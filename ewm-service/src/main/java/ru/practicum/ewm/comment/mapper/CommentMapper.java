package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentDto;
import ru.practicum.ewm.comment.model.CommentStatus;
import ru.practicum.ewm.comment.model.NewCommentDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        commentDto.setEvent(EventMapper.toEventShortDto(comment.getEvent()));
        commentDto.setStatus(comment.getStatus());
        commentDto.setCreatedOn(comment.getCreatedOn().format(FORMATTER));
        return commentDto;
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
}