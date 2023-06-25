package ru.practicum.ewm.util.exception.request;

public class RequestValidationException extends RuntimeException {
    String reason = "Некорректное действие.";

    public RequestValidationException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}