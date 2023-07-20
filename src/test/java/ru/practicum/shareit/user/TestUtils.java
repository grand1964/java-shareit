package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

public class TestUtils {

    public static UserDto createUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }
}
