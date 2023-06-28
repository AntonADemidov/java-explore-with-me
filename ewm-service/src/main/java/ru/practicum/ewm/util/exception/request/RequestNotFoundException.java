package ru.practicum.ewm.util.exception.request;

public class RequestNotFoundException extends RuntimeException {
    String reason = "Запрос на участие в событии отсутствует в базе данных.";

    public RequestNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}