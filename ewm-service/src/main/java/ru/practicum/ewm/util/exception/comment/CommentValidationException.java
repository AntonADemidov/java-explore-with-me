package ru.practicum.ewm.util.exception.comment;

public class CommentValidationException extends RuntimeException {
    String reason = "Некорректое действие: невыполнение условий создания комментария.";

    public CommentValidationException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}