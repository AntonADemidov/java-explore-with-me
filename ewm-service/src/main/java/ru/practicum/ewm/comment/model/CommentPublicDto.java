package ru.practicum.ewm.comment.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.user.model.UserShortDto;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentPublicDto {
    Long id;
    Long authorId;
    String publishedOn;
    String text;
    Integer views;
}
