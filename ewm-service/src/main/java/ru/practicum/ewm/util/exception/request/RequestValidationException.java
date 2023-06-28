package ru.practicum.ewm.util.exception.request;

public class RequestValidationException extends RuntimeException {
    String reason = "Некорректое действие: текущий статус жизненного цикла запроса/события не позволяет вносить изменения в запрос/событие.";

    public RequestValidationException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}