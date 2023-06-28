package ru.practicum.ewm;

public class DateValidationException extends RuntimeException {
    String reason = "Некорректные параметры даты и времени.";

    public DateValidationException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}