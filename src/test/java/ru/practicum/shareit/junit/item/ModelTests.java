package ru.practicum.shareit.junit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ModelTests {
    private final String itemName = "notebook";
    private final String itemDescription = "ASUS";

    //////////////////////////// Тест модели вещи ////////////////////////////

    @Test
    public void comparingItemsTest() {
        //создаем владельца вещи
        User owner = new User(1L, "Vasya", "vasya@com");
        //создаем вещи
        Item item1 = new Item(1L, itemName, itemDescription, true, owner, null);
        Item item2 = new Item(2L, itemName, itemDescription, true, owner, null);
        Item item3 = new Item(1L, "smartphone", "Samsung", false, owner, null);
        assertEquals(item1.hashCode(), 1);
        assertEquals(item1, item3);
        assertNotEquals(item1, item2);
    }

    //////////////////////// Тесты модели комментария ////////////////////////

    @Test
    public void comparingCommentsTest() {
        //создаем владельца вещи
        User owner = new User(1L, "Vasya", "vasya@com");
        //создаем заказчика
        User booker = new User(2L, "Petya", "petya@com");
        //создаем вещь
        Item item = new Item(1L, itemName, itemDescription, true, owner, null);
        //создаем комментарии
        Timestamp created1 = Timestamp.from(Instant.now().plusSeconds(3600));
        Timestamp created2 = Timestamp.from(Instant.now().plusSeconds(7200));
        Comment comment1 = new Comment(1L, "Comment", created1, item, booker);
        Comment comment2 = new Comment(2L, "Comment", created1, item, booker);
        Comment comment3 = new Comment(1L, "New commment", created2, item, booker);
        assertEquals(comment1.hashCode(), 1);
        assertEquals(comment1, comment3);
        assertNotEquals(comment1, comment2);
    }
}
