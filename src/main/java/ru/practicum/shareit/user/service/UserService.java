package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUserById(long id);

    List<UserDto> getAllUsers();

    UserDto createUser(UserDto userDto);

    UserDto patchUser(long id, UserDto userDto);

    void deleteUser(long id);

    void deleteAllUsers();
}
