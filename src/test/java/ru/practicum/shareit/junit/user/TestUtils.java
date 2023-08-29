package ru.practicum.shareit.junit.user;

import ru.practicum.shareit.user.dto.UserInDto;
import ru.practicum.shareit.user.model.User;

public class TestUtils {

    public static UserInDto createUserDto(String name, String email) {
        return new UserInDto(name, email);
    }

    public static User createUser(long id, String name, String email) {
        return new User(id, name, email);
    }
}
