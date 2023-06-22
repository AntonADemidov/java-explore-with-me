package ru.practicum.ewm.user;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.model.NewUserRequest;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserDto;

import java.util.List;

@Transactional(readOnly = true)
public interface UserService {
    @Transactional
    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    @Transactional
    void deleteUserById(Long id);

    User getUserById(Long id);
}