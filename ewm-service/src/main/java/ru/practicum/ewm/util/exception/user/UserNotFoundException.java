package ru.practicum.ewm.util.exception.user;

public class UserNotFoundException extends RuntimeException {
    String reason = "Пользователь отсутствует в базе данных.";

    public UserNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}