package ru.practicum.shareit.junit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class RequestTests {
    private final long authorId = 1L;
    private final long userId = 2L;
    private final long requestId = 1L;
    private ItemRequestService requestService;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemRequestRepository requestRepository;

    @BeforeEach
    public void setUp() {
        requestService = new ItemRequestServiceImpl(userRepository, itemRepository, requestRepository);
    }

    ////////////////////////////// Тесты модели //////////////////////////////

    @Test
    public void comparingBookingsTest() {
        //создаем заказчиков
        User author1 = new User(authorId, "Vasya", "vasya@com");
        User author2 = new User(userId, "Petya", "petya@com");
        //создаем даты
        Timestamp created1 = Timestamp.from(Instant.now().plusSeconds(3600));
        Timestamp created2 = Timestamp.from(Instant.now().plusSeconds(7200));
        //создаем запросы
        ItemRequest request1 = new ItemRequest(1L, "Request1", author1, created1);
        ItemRequest request2 = new ItemRequest(2L, "Request1", author1, created1);
        ItemRequest request3 = new ItemRequest(1L, "Request2", author2, created2);
        assertEquals(request1.hashCode(), 1);
        assertEquals(request1, request3);
        assertNotEquals(request1, request2);
    }

    ///////////////////////////// Тесты создания /////////////////////////////

    @Test
    public void creatingRequestWithBadAuthorTest() {
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> requestService.createRequest(authorId,
                TestUtils.createRequestDto("Bad request")));
    }

    ///////////////////////////// Тесты запросов /////////////////////////////

    @Test
    public void requestByIdWithBadUserTest() {
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> requestService.get(requestId, userId));
    }

    @Test
    public void requestByIdWithBadRequestIdTest() {
        Mockito
                .when(userRepository.findById(authorId))
                .thenReturn(Optional.of(new User(authorId, "Vasya", "vasya@com")));
        Mockito
                .when(requestRepository.findById(requestId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> requestService.get(requestId, authorId));
    }

    @Test
    public void requestByOwnerWithBadUserTest() {
        Mockito
                .when(userRepository.findById(authorId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> requestService.getAllByOwner(authorId));
    }
}
