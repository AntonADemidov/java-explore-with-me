package ru.practicum.ewm.util;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.ApiError;
import ru.practicum.ewm.DateValidationException;
import ru.practicum.ewm.util.exception.category.CategoryNotFoundException;
import ru.practicum.ewm.util.exception.comment.CommentValidationException;
import ru.practicum.ewm.util.exception.compilation.CompilationNotFoundException;
import ru.practicum.ewm.util.exception.event.EventNotFoundException;
import ru.practicum.ewm.util.exception.event.EventValidationException;
import ru.practicum.ewm.util.exception.request.RequestNotFoundException;
import ru.practicum.ewm.util.exception.request.RequestValidationException;
import ru.practicum.ewm.util.exception.user.UserNotFoundException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ErrorHandler {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        String reason = "Некорректные данные в теле запроса.";
        log.error(exception.getMessage());
        return getApiError(HttpStatus.BAD_REQUEST, reason, exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(final MissingServletRequestParameterException exception) {
        String reason = "Некорректные параметры запроса.";
        log.error(exception.getMessage());
        return getApiError(HttpStatus.BAD_REQUEST, reason, exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(final ConstraintViolationException exception) {
        String reason = "Некорректные параметры запроса.";
        log.error(exception.getMessage());
        return getApiError(HttpStatus.BAD_REQUEST, reason, exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateValidationException(final DateValidationException exception) {
        log.error(exception.getMessage());
        return getApiError(HttpStatus.BAD_REQUEST, exception.getLocalizedMessage(), exception.getMessage());
    }

    @ExceptionHandler({UserNotFoundException.class, CategoryNotFoundException.class, EventNotFoundException.class,
            RequestNotFoundException.class, CompilationNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final RuntimeException exception) {
        log.error(exception.getMessage());
        return getApiError(HttpStatus.NOT_FOUND, exception.getLocalizedMessage(), exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlePSQLException(final PSQLException exception) {
        String reason = "Нарушение целостности данных.";
        log.error(exception.getMessage());
        return getApiError(HttpStatus.CONFLICT, reason, exception.getMessage());
    }

    @ExceptionHandler({EventValidationException.class, RequestValidationException.class, CommentValidationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleValidationException(final RuntimeException exception) {
        log.error(exception.getMessage());
        return getApiError(HttpStatus.CONFLICT, exception.getLocalizedMessage(), exception.getMessage());
    }

    private ApiError getApiError(HttpStatus httpStatus, String reason, String message) {
        return new ApiError(LocalDateTime.now().format(FORMATTER), httpStatus, reason, message);
    }
}
