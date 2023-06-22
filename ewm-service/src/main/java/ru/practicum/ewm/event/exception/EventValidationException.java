package ru.practicum.ewm.event.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventValidationException extends RuntimeException {
    public EventValidationException(String message) {
        super(message);
    }
}