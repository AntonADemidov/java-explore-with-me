package ru.practicum.ewm.util.exception.category;

public class CategoryNotFoundException extends RuntimeException {
    String reason = "Категория отсутствует в базе данных.";

    public CategoryNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}