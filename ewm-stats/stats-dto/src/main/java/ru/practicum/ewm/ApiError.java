package ru.practicum.ewm;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ApiError {
    String timestamp;
    HttpStatus status;
    String reason;
    String message;
}