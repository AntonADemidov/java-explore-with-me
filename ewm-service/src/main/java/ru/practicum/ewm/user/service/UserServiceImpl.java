package ru.practicum.ewm.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.PageNumber;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.model.NewUserRequest;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserDto;
import ru.practicum.ewm.user.exception.UserNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User user = userRepository.save(UserMapper.toUser(newUserRequest));
        UserDto userDto = UserMapper.toUserDto(user);
        log.info("Новый пользователь добавлен в базу: {} c id #{}.", userDto.getName(), userDto.getId());
        return userDto;
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable request = PageRequest.of(PageNumber.get(from, size), size);

        if (ids == null) {
            return getUserList(userRepository.findAll(request));
        } else {
            return getUserList(userRepository.findAllByIdIn(ids, request));
        }
    }

    private List<UserDto> getUserList(Page<User> requestPage) {
        List<User> users = requestPage.getContent();

        List<UserDto> userDtos = users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());

        log.info("Список пользователей сформирован: количество элементов={}.", userDtos.size());
        return userDtos;
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        log.info("Из базы удален пользователь: {} c id #{}.", user.getName(), user.getId());
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException(String.format("Пользователь с id #%d отсутствует в базе.", id)));
    }
}