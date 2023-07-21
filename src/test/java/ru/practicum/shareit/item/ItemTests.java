package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.TestUtils;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.item.TestUtils.createItemDto;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemTests {
    private final UserService userService;
    private final ItemService itemService;

    @BeforeEach
    public void initData() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.createUser(TestUtils.createUserDto("Petya", "petya@com"));
        userService.createUser(TestUtils.createUserDto("Fedya", "fedya@com"));
    }

    @AfterEach
    public void resetData() {
        userService.deleteAllUsers();
        itemService.deleteAllItems();
    }

    @Test
    public void createItemTest() {
        long ownerId = 1L;
        itemService.createItem(ownerId, createItemDto("notebook", "ASUS", true));
        itemService.createItem(ownerId, createItemDto("drill", "Makita", null));
        List<ItemDto> items = itemService.getAllItems(ownerId);
        assertEquals(items.size(), 2);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(0).getAvailable(), true);
        assertEquals(items.get(1).getDescription(), "Makita");
        assertNull(items.get(1).getAvailable());
    }

    @Test
    public void createWithNotExistingOwnerTest() {
        assertThrows(NotFoundException.class, () -> itemService.createItem(
                10L, createItemDto("notebook", "ASUS", true)));
    }

    @Test
    public void patchItemWithNotExistingOwnerTest() {
        itemService.createItem(1L, createItemDto("notebook", "ASUS", true));
        assertThrows(NotFoundException.class, () -> itemService.patchItem(10L,
                createItemDto("notebook", "ASUS", true)));
    }

    @Test
    public void patchItemByForeignOwnerTest() {
        itemService.createItem(1L, createItemDto("notebook", "ASUS", true));
        ItemDto itemDto = createItemDto("notebook", "ASUS", true);
        itemDto.setId(1L);
        assertThrows(ForbiddenException.class, () -> itemService.patchItem(2L, itemDto));
    }

    @Test
    public void patchItemWithNameTest() {
        long itemId = 1L;
        itemService.createItem(itemId, createItemDto("notebook", "ASUS", true));
        ItemDto itemDto = createItemDto("smartphone", null, null);
        itemDto.setId(itemId);
        itemService.patchItem(1L, itemDto);
        itemDto = itemService.getItem(itemId);
        assertEquals(itemDto.getName(), "smartphone");
        assertEquals(itemDto.getDescription(), "ASUS");
        assertTrue(itemDto.getAvailable());
    }

    @Test
    public void patchItemWithDescriptionTest() {
        long itemId = 1L;
        itemService.createItem(itemId, createItemDto("notebook", "ASUS", true));
        ItemDto itemDto = createItemDto(null, "Sony", null);
        itemDto.setId(itemId);
        itemService.patchItem(1L, itemDto);
        itemDto = itemService.getItem(itemId);
        assertEquals(itemDto.getName(), "notebook");
        assertEquals(itemDto.getDescription(), "Sony");
        assertTrue(itemDto.getAvailable());
    }

    @Test
    public void patchItemWithAvailableTest() {
        long itemId = 1L;
        itemService.createItem(itemId, createItemDto("notebook", "ASUS", true));
        ItemDto itemDto = createItemDto(null, null, false);
        itemDto.setId(itemId);
        itemService.patchItem(1L, itemDto);
        itemDto = itemService.getItem(itemId);
        assertEquals(itemDto.getName(), "notebook");
        assertEquals(itemDto.getDescription(), "ASUS");
        assertFalse(itemDto.getAvailable());
    }

    @Test
    public void getAllItemsTest() {
        //создаем несколько вещей одного владельца
        long ownerId = 1L;
        itemService.createItem(ownerId, createItemDto("notebook", "ASUS", true));
        itemService.createItem(ownerId, createItemDto("smartphone", "Samsung", true));
        itemService.createItem(ownerId, createItemDto("computer", "Apple", false));
        //и вещь другого
        itemService.createItem(2L, createItemDto("book", "About Spring ", true));
        //получаем список владельца 1
        List<ItemDto> items = itemService.getAllItems(ownerId);
        assertEquals(items.size(), 3);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(1).getDescription(), "Samsung");
        assertFalse(items.get(2).getAvailable());
    }

    @Test
    public void searchTest() {
        //создаем несколько вещей разных владельцев
        itemService.createItem(1L, createItemDto("notebook", "ASUS", true));
        itemService.createItem(1L, createItemDto("notebook", "Samsung", false));
        itemService.createItem(3L, createItemDto("TeXBook", "By Knuth", true));
        itemService.createItem(3L, createItemDto("About Spring",
                "The book about java", true));
        itemService.createItem(2L, createItemDto("computer", "Apple", true));
        itemService.createItem(2L, createItemDto("book", "About Spring ", false));
        //выполняем поиск
        List<ItemDto> items = itemService.searchItems("bOok");
        //проверяем результат
        assertEquals(items.size(), 3);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(1).getDescription(), "By Knuth");
        assertEquals(items.get(2).getName(), "About Spring");
    }
}
