package ru.practicum.shareit.integral.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemTests {
    private final UserService userService;
    private final ItemService itemService;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        userService.createUser(TestUtils.createUserInDto("Vasya", "vasya@com"));
        userService.createUser(TestUtils.createUserInDto("Petya", "petya@com"));
        userService.createUser(TestUtils.createUserInDto("Fedya", "fedya@com"));
    }

    @AfterEach
    public void resetData() {
        jdbcTemplate.update(TestUtils.getSqlForReset());
    }

    @Test
    public void createItemTest() {
        long ownerId = 1L;
        int from = 0;
        int size = 20;
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("drill", "Makita", null));
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, from, size);
        assertEquals(items.size(), 2);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(0).getAvailable(), true);
        assertEquals(items.get(1).getDescription(), "Makita");
        assertNull(items.get(1).getAvailable());
    }

    @Test
    public void getAllItemsTest() {
        //создаем несколько вещей одного владельца
        long ownerId = 1L;
        int from = 0;
        int size = 20;
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("smartphone", "Samsung", true));
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("computer", "Apple", false));
        //и вещь другого
        itemService.createItem(2L,
                TestUtils.createItemInDto("book", "About Spring ", true));
        //получаем список владельца 1
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, from, size);
        assertEquals(items.size(), 3);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(1).getDescription(), "Samsung");
        assertFalse(items.get(2).getAvailable());
    }
}
