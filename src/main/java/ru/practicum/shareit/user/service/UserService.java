package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserOutDto;
import ru.practicum.shareit.user.dto.UserInDto;

import java.util.List;

public interface UserService {
    UserOutDto getUserById(long id);

    List<UserOutDto> getAllUsers();

    UserOutDto createUser(UserInDto userInDto);

    UserOutDto patchUser(long id, UserInDto userInDto);

    void deleteUser(long id);

    void deleteAllUsers();
}
