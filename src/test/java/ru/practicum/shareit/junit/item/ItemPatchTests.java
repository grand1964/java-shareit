package ru.practicum.shareit.junit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static ru.practicum.shareit.junit.item.TestUtils.createItemInDto;

@ExtendWith(MockitoExtension.class)
public class ItemPatchTests {
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
        //метод save возвращает того же пользователя с предписанным id
        Mockito
                .when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocationOnMock -> {
                    Item item = invocationOnMock.getArgument(0, Item.class);
                    item.setId(itemId);
                    return item;
                });
    }

    //////////////////////////// Тесты обновления ////////////////////////////

    @Test
    public void patchItemWithNotExistingOwnerTest() {
        long ownerId = 1L;
        long newOwnerId = 10L;
        User owner = TestUtils.createUser(ownerId, userName, email);
        //метод findById с аргументом ownerId возвращает владельца
        Mockito
                .when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        //метод findById с аргументом ownerId генерирует ошибку ненайденного владельца
        Mockito
                .when(userRepository.findById(newOwnerId))
                .thenThrow(new NotFoundException("Недопустимый владелец"));
        itemService.createItem(ownerId, createItemInDto(itemName, itemDescription, true));
        assertThrows(NotFoundException.class, () -> itemService.patchItem(newOwnerId,
                createItemInDto(itemName, itemDescription, true)));
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
    }

    @Test
    public void patchItemByForeignOwnerTest() {
        long ownerId = 1L;
        long userId = 2L;
        Long itemId = 1L;
        User owner = TestUtils.createUser(ownerId, userName, email);
        //метод userRepository.findById возвращает владельца
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long id = invocationOnMock.getArgument(0, Long.class);
                    return Optional.of(TestUtils.createUser(id, userName, email));
                });
        //метод itemRepository.findById возвращает созданную вещь
        Mockito
                .when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(
                        new Item(itemId, itemName, itemDescription, true, owner, null)
                ));
        ItemInDto itemInDto = createItemInDto(itemName, itemDescription, true);
        itemService.createItem(ownerId, itemInDto);
        itemInDto.setId(ownerId);
        assertThrows(ForbiddenException.class, () -> itemService.patchItem(userId, itemInDto));
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
    }

    @Test
    public void patchItemWithNameTest() {
        long ownerId = 1L;
        long itemId = 1L;
        User owner = TestUtils.createUser(ownerId, userName, email);
        //метод userRepository.findById возвращает владельца
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long id = invocationOnMock.getArgument(0, Long.class);
                    return Optional.of(TestUtils.createUser(id, userName, email));
                });
        //метод itemRepository.findById возвращает созданную вещь
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(
                        new Item(itemId, itemName, itemDescription, true, owner, null)
                ));
        itemService.createItem(itemId, createItemInDto(itemName, itemDescription, true));
        ItemInDto itemInDto = createItemInDto("smartphone", null, null);
        itemInDto.setId(itemId);
        itemService.patchItem(ownerId, itemInDto);
        ItemOutBookedDto itemOutDto = itemService.getItem(itemId, ownerId);
        assertEquals(itemOutDto.getName(), "smartphone");
        assertEquals(itemOutDto.getDescription(), "ASUS");
        assertTrue(itemOutDto.getAvailable());
        Mockito.verify(itemRepository, Mockito.times(2)).save(any(Item.class));
    }

    @Test
    public void patchItemWithDescriptionTest() {
        long ownerId = 1L;
        long itemId = 1L;
        User owner = TestUtils.createUser(ownerId, userName, email);
        //метод userRepository.findById возвращает владельца
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long id = invocationOnMock.getArgument(0, Long.class);
                    return Optional.of(TestUtils.createUser(id, userName, email));
                });
        //метод itemRepository.findById возвращает созданную вещь
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(
                        new Item(itemId, itemName, itemDescription, true, owner, null)
                ));
        itemService.createItem(itemId, createItemInDto(itemName, itemDescription, true));
        ItemInDto itemInDto = createItemInDto(null, "Sony", null);
        itemInDto.setId(itemId);
        itemService.patchItem(ownerId, itemInDto);
        ItemOutBookedDto itemOutDto = itemService.getItem(itemId, ownerId);
        assertEquals(itemOutDto.getName(), "notebook");
        assertEquals(itemOutDto.getDescription(), "Sony");
        assertTrue(itemOutDto.getAvailable());
        Mockito.verify(itemRepository, Mockito.times(2)).save(any(Item.class));
    }

    @Test
    public void patchItemWithAvailableTest() {
        long ownerId = 1L;
        long itemId = 1L;
        User owner = TestUtils.createUser(ownerId, userName, email);
        //метод userRepository.findById возвращает владельца
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long id = invocationOnMock.getArgument(0, Long.class);
                    return Optional.of(TestUtils.createUser(id, userName, email));
                });
        //метод itemRepository.findById возвращает созданную вещь
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(
                        new Item(itemId, itemName, itemDescription, true, owner, null)
                ));
        itemService.createItem(itemId, createItemInDto(itemName, itemDescription, true));
        ItemInDto itemInDto = createItemInDto(null, null, false);
        itemInDto.setId(itemId);
        itemService.patchItem(ownerId, itemInDto);
        ItemOutBookedDto itemOutDto = itemService.getItem(itemId, ownerId);
        assertEquals(itemOutDto.getName(), "notebook");
        assertEquals(itemOutDto.getDescription(), "ASUS");
        assertFalse(itemOutDto.getAvailable());
        Mockito.verify(itemRepository, Mockito.times(2)).save(any(Item.class));
    }
}
