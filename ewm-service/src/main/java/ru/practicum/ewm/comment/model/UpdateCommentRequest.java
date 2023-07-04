package ru.practicum.ewm.comment.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCommentRequest {
    @NotBlank
    String text;
}