package ru.practicum.shareit.junit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ModelTests {
    @Test
    public void comparingItemsTest() {
        //создаем пользователей
        User user1 = new User(1L, "Vasya", "vasya@com");
        User user2 = new User(2L, "Vasya", "vasya@com");
        User user3 = new User(1L, "Fedya", "fedya@com");
        assertEquals(user1.hashCode(), 1);
        assertEquals(user1, user3);
        assertNotEquals(user1, user2);
    }
}
