package ru.practicum.ewm.comment.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentStatusUpdateResult {
    List<CommentDto> publishedComments;
    List<CommentDto> rejectedComments;
}