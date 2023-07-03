package ru.practicum.ewm.comment.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.model.EventShortDto;
import ru.practicum.ewm.user.model.UserShortDto;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    Long eventId;
    String text;
    UserShortDto author;
    CommentStatus status;
    String createdOn;
    String publishedOn;
    List<MessageDto> messages;
}