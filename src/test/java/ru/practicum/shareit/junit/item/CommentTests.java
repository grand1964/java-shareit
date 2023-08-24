package ru.practicum.shareit.junit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.CommentServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentTests {
    private final long itemId = 1L;
    private final String itemName = "notebook";
    private final String itemDescription = "ASUS";
    private final String userName = "Vasya";
    private final String email = "vasya@com";
    private final long ownerId = 1L;
    private final long bookerId = 2L;
    private final long userId = 3L;
    private CommentService commentService;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;

    @BeforeEach
    public void setUp() {
        //создаем службу с mock-репозиториями
        commentService = new CommentServiceImpl(userRepository, itemRepository,
                bookingRepository, commentRepository);
    }

    ///////////////////////////// Тесты создания /////////////////////////////

    @Test
    public void createCommentByNoBookerOrBadBookerTest() {
        long badUserId = 100L;
        //создаем пользователей
        User owner = TestUtils.createUser(ownerId, userName, email); //владельца
        User booker = TestUtils.createUser(bookerId, userName, email); //заказчика
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        //задаем подтвержденное бронирование
        Booking booking = new Booking(bookerId, TestUtils.fromOffset(-2L), TestUtils.fromOffset(-1L),
                item, booker, Status.APPROVED);
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
        //имитируем результат запроса прошлых подтвержденных бронирований
        Mockito
                .when(bookingRepository.findByBooker_IdAndEndBeforeAndStatusIs(
                        anyLong(), any(Timestamp.class), any(Status.class)))
                .thenAnswer(invocationOnMock -> {
                    Long id = invocationOnMock.getArgument(0, Long.class);
                    if (id == bookerId) {
                        return List.of(booking);
                    } else {
                        return List.of();
                    }
                });
        //комментарий от другого лица - ошибка
        assertThrows(BadRequestException.class,
                () -> commentService.createComment(itemId, userId,
                        TestUtils.createCommentInDto("Комментарий"))
        );
        //теперь метод userRepository.findById возвращает отсутствующего пользователя
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        //комментарий от несуществующего пользователя - тоже ошибка
        assertThrows(NotFoundException.class,
                () -> commentService.createComment(itemId, badUserId,
                        TestUtils.createCommentInDto("Комментарий"))
        );
    }

    @Test
    public void createCommentToFutureBookingTest() {
        //создаем пользователей
        User owner = TestUtils.createUser(ownerId, userName, email); //владельца
        User booker = TestUtils.createUser(bookerId, userName, email); //заказчика
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        //задаем список подтвержденных бронирований
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(bookerId, TestUtils.fromOffset(-1L), TestUtils.fromOffset(1L),
                item, booker, Status.APPROVED)); //текущее
        bookings.add(new Booking(bookerId, TestUtils.fromOffset(1L), TestUtils.fromOffset(2L),
                item, booker, Status.APPROVED)); //на будущее
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
        //результат запроса подтвержденных бронирований принудительно задаем пустым
        Mockito
                .when(bookingRepository.findByBooker_IdAndEndBeforeAndStatusIs(
                        anyLong(), any(Timestamp.class), any(Status.class)))
                .thenAnswer(invocationOnMock -> {
                    Timestamp now = invocationOnMock.getArgument(1, Timestamp.class);
                    return bookings.stream().filter(b -> b.getEnd().before(now)).collect(Collectors.toList());
                });
        //комментарий создать нельзя
        assertThrows(BadRequestException.class,
                () -> commentService.createComment(itemId, userId,
                        TestUtils.createCommentInDto("Комментарий"))
        );
    }
}
