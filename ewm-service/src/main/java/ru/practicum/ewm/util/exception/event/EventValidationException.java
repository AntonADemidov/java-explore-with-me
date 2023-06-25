package ru.practicum.ewm.util.exception.event;

public class EventValidationException extends RuntimeException {
    String reason = "Некорректное действие.";

    public EventValidationException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}