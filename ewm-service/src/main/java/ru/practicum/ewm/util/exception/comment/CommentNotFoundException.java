package ru.practicum.ewm.util.exception.comment;

public class CommentNotFoundException extends RuntimeException {
    String reason = "Комментарий к событию отсутствует в базе данных.";

    public CommentNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}