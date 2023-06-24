package ru.practicum.ewm.event.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DateValidationException extends RuntimeException {
    public DateValidationException(String message) {
        super(message);
    }
}