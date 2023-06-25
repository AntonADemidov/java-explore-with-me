package ru.practicum.ewm.util.exception.event;

public class EventDateValidationException extends RuntimeException {
    String reason = "Некорректные параметры даты и времени.";

    public EventDateValidationException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}