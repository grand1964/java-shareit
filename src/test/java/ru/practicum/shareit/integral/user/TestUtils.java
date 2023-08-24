package ru.practicum.shareit.integral.user;

import ru.practicum.shareit.user.dto.UserInDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class TestUtils {
    public static UserInDto createUserInDto(String name, String email) {
        return new UserInDto(name, email);
    }

    public static String getSqlForResetUsers() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DROP TABLE users, items CASCADE; ");
        try {
            stringBuilder.append(new String(Files.readAllBytes(
                    Path.of("./src/main/resources/schema.sql"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }
}
