package ru.practicum.ewm.util.exception.event;

public class EventNotFoundException extends RuntimeException {
    String reason = "Событие отсутствует в базе данных.";

    public EventNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}