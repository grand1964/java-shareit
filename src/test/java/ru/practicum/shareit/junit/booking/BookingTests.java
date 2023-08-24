package ru.practicum.shareit.junit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class BookingTests {
    private final long ownerId = 1L;
    private final long bookerId = 2L;
    private final long itemId = 1L;
    private final long bookingId = 1L;
    private final String userName = "Vasya";
    private final String email = "vasya@com";
    private final String itemName = "notebook";
    private final String itemDescription = "ASUS";
    private BookingService bookingService;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(userRepository, itemRepository, bookingRepository);
    }

    ///////////////////////////// Тесты создания /////////////////////////////

    @Test
    public void createBookingOnSelfItemTest() {
        //создаем владельца вещи
        User owner = new User(ownerId, userName, email);
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookerId, bookingDto));
    }

    @Test
    public void createBookingWithNonExistingItemTest() {
        //принудительно возвращаем несуществующую вещь
        Mockito
                .when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookerId, bookingDto));
    }

    @Test
    public void createBookingWithNonExistingBookerTest() {
        //создаем владельца вещи
        User owner = new User(ownerId, userName, email);
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        //принудительно возвращаем несуществующего заказчика
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.empty());
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookerId, bookingDto));
    }

    @Test
    public void createBookingWithNonAvailableItemTest() {
        //создаем владельца вещи
        User owner = new User(ownerId, userName, email);
        //создаем недоступную вещь
        Item item = new Item(itemId, itemName, itemDescription, false, owner, null);
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(bookerId, bookingDto));
    }

    @Test
    public void createBookingWithStartAfterEndTest() {
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 2L, 1L);
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(bookerId, bookingDto));
    }

    /////////////////////////// Тесты подтверждения //////////////////////////

    @Test
    public void confirmApprovingFromBadUserTest() {
        long normalUserId = 3L;
        long badUserId = 100L;
        //создаем владельца вещи
        User owner = new User(ownerId, userName, email);
        //создаем заказчика вещи
        User booker = new User(bookerId, userName, email);
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        //задаем неподтвержденное бронирование вещи
        Booking booking = new Booking(bookingId,
                TestUtils.fromOffset(1L), TestUtils.fromOffset(2L),
                item, booker, Status.WAITING);
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        //подтверждение от постороннего пользователя
        assertThrows(BadRequestException.class,
                () -> bookingService.confirmBooking(bookingId, normalUserId, true));
        //подтверждение от несуществующего пользователя
        assertThrows(BadRequestException.class,
                () -> bookingService.confirmBooking(bookingId, badUserId, true));
        //подтверждение от самого заказчика
        assertThrows(NotFoundException.class,
                () -> bookingService.confirmBooking(bookingId, bookerId, true));
    }

    @Test
    public void confirmApprovingNotWaitingBookingTest() {
        //создаем владельца вещи
        User owner = new User(ownerId, userName, email);
        //создаем заказчика вещи
        User booker = new User(bookerId, userName, email);
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        //задаем подтвержденное бронирование вещи
        Booking booking = new Booking(bookingId,
                TestUtils.fromOffset(1L), TestUtils.fromOffset(2L),
                item, booker, Status.APPROVED);
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        //подтверждение уже подтвержденной брони
        assertThrows(BadRequestException.class,
                () -> bookingService.confirmBooking(bookingId, ownerId, true));
        //подтверждение отвергнутой брони
        booking.setStatus(Status.REJECTED);
        assertThrows(BadRequestException.class,
                () -> bookingService.confirmBooking(bookingId, ownerId, true));
    }

    @Test
    public void confirmOfOverlappedBookingTest() {
        long bookingId2 = 2L;
        long bookingId3 = 3L;
        long bookingId4 = 4L;
        //создаем владельца вещи
        User owner = new User(ownerId, userName, email);
        //создаем заказчика вещи
        User booker = new User(bookerId, userName, email);
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        //создаем массив бронирований для вещи
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(bookingId2,
                TestUtils.fromOffset(1L), TestUtils.fromOffset(3L),
                item, booker, Status.APPROVED));
        bookings.add(new Booking(bookingId3,
                TestUtils.fromOffset(11L), TestUtils.fromOffset(13L),
                item, booker, Status.APPROVED));
        bookings.add(new Booking(bookingId4,
                TestUtils.fromOffset(21L), TestUtils.fromOffset(23L),
                item, booker, Status.APPROVED));
        Mockito
                .when(bookingRepository.findByItem_IdAndEndAfterAndStartBeforeAndStatusIs(
                        anyLong(), any(Timestamp.class), any(Timestamp.class), any(Status.class)))
                .thenAnswer(invocationOnMock -> {
                    Timestamp start = invocationOnMock.getArgument(1, Timestamp.class);
                    Timestamp end = invocationOnMock.getArgument(2, Timestamp.class);
                    Status status = invocationOnMock.getArgument(3, Status.class);
                    return bookings.stream()
                            .filter(b -> b.getStart().before(end)
                                    && b.getEnd().after(start)
                                    && status == Status.APPROVED)
                            .collect(Collectors.toList());
                });

        //одобрение накладывающихся бронирований должно давать ошибку
        //наложение слева
        Booking booking = new Booking(bookingId,
                TestUtils.fromOffset(2L), TestUtils.fromOffset(4L),
                item, booker, Status.WAITING);
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class,
                () -> bookingService.confirmBooking(bookingId, ownerId, true));
        //наложение справа
        booking = new Booking(bookingId4,
                TestUtils.fromOffset(18L), TestUtils.fromOffset(22L),
                item, booker, Status.WAITING);
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class,
                () -> bookingService.confirmBooking(bookingId, ownerId, true));

        //а отвергнуть налегающую бронь можно
        booking = new Booking(bookingId,
                TestUtils.fromOffset(1L), TestUtils.fromOffset(32L),
                item, booker, Status.WAITING);
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        //задаем метод сохранения
        Mockito
                .when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Booking.class));
        bookingService.confirmBooking(bookingId, ownerId, false);
        BookingOutDto bookingOutDto = bookingService.getBookingById(bookingId, ownerId);
        assertEquals(bookingOutDto.getStatus(), Status.REJECTED);
        Mockito.verify(bookingRepository, Mockito.times(1)).save(any(Booking.class));
    }

    /////////////////////////// Тесты чтения по id ///////////////////////////

    @Test
    public void getBookingByBadUserTest() {
        long userId = 3;
        //создаем владельца вещи
        User owner = new User(ownerId, userName, email);
        //создаем заказчика вещи
        User booker = new User(bookerId, userName, email);
        //создаем вещь
        Item item = new Item(itemId, itemName, itemDescription, true, owner, null);
        Booking booking = new Booking(bookingId,
                TestUtils.fromOffset(1L), TestUtils.fromOffset(2L), item, booker, Status.APPROVED);
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingId, userId));
    }
}
