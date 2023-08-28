package ru.practicum.shareit.junit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GetItemTests {
    private final long itemId = 1L;
    private final String itemName = "notebook";
    private final String itemDescription = "ASUS";
    private final String userName = "Vasya";
    private final String email = "vasya@com";
    private ItemService itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemRequestRepository requestRepository;

    @BeforeEach
    public void setUp() {
        //создаем службу с mock-репозиториями
        itemService = new ItemServiceImpl(itemRepository, userRepository, commentRepository, requestRepository);
    }

    /////////////////////////// Тесты чтения вещей ///////////////////////////

    @Test
    public void getAllItemsWithBadOwnerTest() {
        long ownerId = 1L;
        //метод findById с аргументом ownerId возвращает владельца
        Mockito
                .when(userRepository.existsById(ownerId))
                .thenReturn(false);
        PageRequest pageable = PageRequest.of(0, 20);
        assertThrows(NotFoundException.class, () -> itemService.getAllItems(ownerId,
                PageRequest.of(0, 20)));
    }

    @Test
    public void getItemByIdWithBadItemTest() {
        long userId = 2L;
        Mockito
                .when(itemRepository.findById(itemId))
               .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.getItem(itemId, userId));
    }

    @Test
    public void getItemByIdWithoutBookingsAndCommentsTest() {
        long ownerId = 1L;
        long userId = 2L;
        Timestamp now = Timestamp.from(Instant.now());
        //создаем владельца
        User owner = TestUtils.createUser(ownerId, userName, email);
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Mockito
                .doReturn(Optional.of(item))
                .when(itemRepository).findById(itemId);
        ItemOutBookedDto dto = itemService.getItem(itemId,userId);
        assertEquals(dto.getId(), itemId);
        assertEquals(dto.getName(), itemName);
        assertEquals(dto.getDescription(), itemDescription);
        assertTrue(dto.getAvailable());
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        assertEquals(dto.getComments().size(), 0);
    }
}
