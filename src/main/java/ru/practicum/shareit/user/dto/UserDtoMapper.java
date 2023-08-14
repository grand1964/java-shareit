package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserDtoMapper {
    public static UserOutDto toUserOutDto(User user) {
        return new UserOutDto(user.getId(), user.getName(), user.getEmail());
    }

    public static User toUser(UserInDto userInDto) {
        User user = new User();
        user.setName(userInDto.getName());
        user.setEmail(userInDto.getEmail());
        return user;
    }

    public static List<UserOutDto> listToUserDto(List<User> list) {
        return list.stream().map(UserDtoMapper::toUserOutDto).collect(Collectors.toList());
    }
}
