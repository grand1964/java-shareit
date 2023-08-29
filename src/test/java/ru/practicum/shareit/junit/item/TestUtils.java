package ru.practicum.shareit.junit.item;

import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.user.model.User;

import java.sql.Timestamp;
import java.time.Instant;

public class TestUtils {

    public static User createUser(long id, String name, String email) {
        return new User(id, name, email);
    }

    public static ItemInDto createItemInDto(String name, String description, Boolean available) {
        return new ItemInDto(null, name, description, available, null);
    }

    public static CommentInDto createCommentInDto(String text) {
        return new CommentInDto(text);
    }

    public static Timestamp fromOffset(Long days) {
        return Timestamp.from(Instant.now().plusSeconds(days * 24 * 3600));
    }
}
