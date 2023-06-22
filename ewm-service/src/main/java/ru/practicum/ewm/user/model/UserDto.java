package ru.practicum.ewm.user.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    String name;
    String email;
}