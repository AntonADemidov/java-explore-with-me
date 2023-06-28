package ru.practicum.ewm.util.exception.compilation;

public class CompilationNotFoundException extends RuntimeException {
    String reason = "Подборка событий отсутствует в базе данных.";

    public CompilationNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}