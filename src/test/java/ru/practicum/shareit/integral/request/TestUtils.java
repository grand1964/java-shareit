package ru.practicum.shareit.integral.request;

import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.user.dto.UserInDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;

public class TestUtils {

    public static UserInDto createUserInDto(String name, String email) {
        return new UserInDto(name, email);
    }

    public static ItemInDto createItemDto(String name, String description, Boolean available, Long requestId) {
        return new ItemInDto(null, name, description, available, requestId);
    }

    public static ItemRequestInDto createRequestDto(String description) {
        return new ItemRequestInDto(description);
    }

    public static String getSqlForReset() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DROP TABLE users, items, bookings, comments, requests CASCADE; ");
        try {
            stringBuilder.append(new String(Files.readAllBytes(
                    Path.of("./src/main/resources/schema.sql"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    public static Timestamp fromOffset(Long days) {
        return Timestamp.from(Instant.now().plusSeconds((long) days * 24 * 3600));
    }
}
