package ru.practicum.ewm.util.exception.event;

public class EventValidationException extends RuntimeException {
    String reason = "Некорректое действие: текущий статус жизненного цикла не позволяет вносить изменения в событие.";

    public EventValidationException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}