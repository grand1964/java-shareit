package ru.practicum.shareit.integral.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingTests {
    private static final int USER_COUNT = 10;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        //создаем пользователей
        for (int i = 1; i <= USER_COUNT; i++) {
            userService.createUser(TestUtils.createUserInDto("user" + i, "user" + i + "@com"));
        }
        //создаем вещи
        itemService.createItem(1L, TestUtils.createItemInDto("item1", "", false));
        int ind = 1;
        for (int i = 2; i <= 4; i++) {
            for (int j = 1; j <= i; j++) {
                ind++;
                itemService.createItem((long) i,
                        TestUtils.createItemInDto("item" + ind, "", true));
            }
        }
    }

    @AfterEach
    public void resetData() {
        jdbcTemplate.update(TestUtils.getSqlForReset());
    }

    ///////////////////////////// Тесты создания /////////////////////////////

    @Test
    public void createNormalBookingTest() {
        long bookerId = 5L;
        long itemId = 2L;
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        BookingOutDto bookingOutDto = bookingService.createBooking(bookerId, bookingDto);
        assertEquals(bookingOutDto.getBooker().getId(), bookerId);
        assertEquals(bookingOutDto.getItem().getId(), itemId);
        assertEquals(bookingOutDto.getStatus(), Status.WAITING);
    }

    /////////////////////////// Тесты подтверждения //////////////////////////

    @Test
    public void confirmNormalApprovingTest() {
        long bookerId = 5L;
        long ownerId = 2L;
        long itemId = 2L;
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        Long bookingId = bookingService.createBooking(bookerId, bookingDto).getId();
        BookingOutDto bookingOutDto = bookingService.confirmBooking(bookingId, ownerId, true);
        assertEquals(bookingOutDto.getBooker().getId(), bookerId);
        assertEquals(bookingOutDto.getItem().getId(), itemId);
        assertEquals(bookingOutDto.getStatus(), Status.APPROVED);
    }

    @Test
    public void confirmNormalRejectingTest() {
        long bookerId = 5L;
        long ownerId = 2L;
        long itemId = 2L;
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        Long bookingId = bookingService.createBooking(bookerId, bookingDto).getId();
        BookingOutDto bookingOutDto = bookingService.confirmBooking(bookingId, ownerId, false);
        assertEquals(bookingOutDto.getBooker().getId(), bookerId);
        assertEquals(bookingOutDto.getItem().getId(), itemId);
        assertEquals(bookingOutDto.getStatus(), Status.REJECTED);
    }

    /////////////////////////// Тесты чтения по id ///////////////////////////

    @Test
    public void getBookingByBookerTest() {
        long bookerId = 5L;
        long itemId = 2L;
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        Long bookingId = bookingService.createBooking(bookerId, bookingDto).getId();
        BookingOutDto bookingOutDto = bookingService.getBookingById(bookingId, bookerId);
        assertEquals(bookingOutDto.getBooker().getId(), bookerId);
        assertEquals(bookingOutDto.getItem().getId(), itemId);
        assertEquals(bookingOutDto.getStatus(), Status.WAITING);
    }

    @Test
    public void getBookingByOwnerTest() {
        long bookerId = 5L;
        long ownerId = 2L;
        long itemId = 2L;
        BookingInDto bookingDto = TestUtils.createBookingInDto(itemId, 1L, 2L);
        Long bookingId = bookingService.createBooking(bookerId, bookingDto).getId();
        BookingOutDto bookingOutDto = bookingService.getBookingById(bookingId, ownerId);
        assertEquals(bookingOutDto.getBooker().getId(), bookerId);
        assertEquals(bookingOutDto.getItem().getId(), itemId);
        assertEquals(bookingOutDto.getStatus(), Status.WAITING);
    }

    ///////////////////// Тесты чтения броней по статусу /////////////////////

    @Test
    public void getAllBookingsOfBookerByStateTest() {
        long bookerId = 5L;
        long bookingId;
        int from = 0;
        int size = 20;
        //создаем бронь с нужными диапазонами и одобрениями
        bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(2L, 1L, 2L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(3L, 3L, 4L)).getId();
        bookingService.confirmBooking(bookingId, 2L, false);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-1L), TestUtils.fromOffset(1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(4L, 5L, 6L)).getId();
        bookingService.confirmBooking(bookingId, 3L, true);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-2L), TestUtils.fromOffset(-1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(5L, 7L, 8L)).getId();
        bookingService.confirmBooking(bookingId, 3L, false);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(2L), TestUtils.fromOffset(3L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(6L, 9L, 10L)).getId();
        bookingService.confirmBooking(bookingId, 3L, true);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-3L), TestUtils.fromOffset(3L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(7L, 11L, 12L)).getId();
        bookingService.confirmBooking(bookingId, 4L, false);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-5L), TestUtils.fromOffset(-3L));
        //запрашиваем бронирования пользователя по state
        List<BookingOutDto> bookings = bookingService.getAllBookingsForBooker(
                bookerId, "CURRENT", from, size);
        assertEquals(bookings.size(), 2);
        bookings = bookingService.getAllBookingsForBooker(bookerId, "FUTURE", from, size);
        assertEquals(bookings.size(), 2);
        bookings = bookingService.getAllBookingsForBooker(bookerId, "PAST", from, size);
        assertEquals(bookings.size(), 2);
        bookings = bookingService.getAllBookingsForBooker(bookerId, "ALL", from, size);
        assertEquals(bookings.size(), 6);
        bookings = bookingService.getAllBookingsForBooker(bookerId, "WAITING", from, size);
        assertEquals(bookings.size(), 1);
        bookings = bookingService.getAllBookingsForBooker(bookerId, "REJECTED", from, size);
        assertEquals(bookings.size(), 3);
    }

    @Test
    public void getAllBookingsOfOwnerByStateTest() {
        long bookerId = 5L;
        long ownerId = 4L;
        long bookingId;
        int from = 0;
        int size = 20;
        //создаем бронь с нужными диапазонами и одобрениями
        bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(7L, 1L, 2L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(7L, 3L, 4L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, false);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-1L), TestUtils.fromOffset(1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(7L, 5L, 6L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-2L), TestUtils.fromOffset(-1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(8L, 7L, 8L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, false);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(2L), TestUtils.fromOffset(3L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(8L, 9L, 10L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-3L), TestUtils.fromOffset(3L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(8L, 11L, 12L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, false);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-5L), TestUtils.fromOffset(-3L));
        //запрашиваем бронирования вещей владельца по state
        List<BookingOutDto> bookings = bookingService.getAllBookingsForOwner(
                ownerId, "CURRENT", from, size);
        assertEquals(bookings.size(), 2);
        bookings = bookingService.getAllBookingsForOwner(ownerId, "FUTURE", from, size);
        assertEquals(bookings.size(), 2);
        bookings = bookingService.getAllBookingsForOwner(ownerId, "PAST", from, size);
        assertEquals(bookings.size(), 2);
        bookings = bookingService.getAllBookingsForOwner(ownerId, "WAITING", from, size);
        assertEquals(bookings.size(), 1);
        bookings = bookingService.getAllBookingsForOwner(ownerId, "REJECTED", from, size);
        assertEquals(bookings.size(), 3);
        bookings = bookingService.getAllBookingsForOwner(ownerId, "ALL", from, size);
        assertEquals(bookings.size(), 6);
    }

    //////////////// Тесты чтения вещей владельца с границами ////////////////

    @Test
    public void getAllItemsOfOwnerWithoutCurrentTest() {
        long bookerId = 5L;
        long ownerId = 4L;
        long itemId = 7;
        long bookingId;
        int from = 0;
        int size = 20;
        //создаем бронь с нужными диапазонами и одобрениями
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-5L), TestUtils.fromOffset(-4L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        Timestamp lastTime = bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(-3L), TestUtils.fromOffset(-1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        Timestamp nextTime = bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(1L), TestUtils.fromOffset(3L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 4L, 5L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        //читаем список вещей владельца
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, from, size);
        assertEquals(items.size(), 4);
        assertEquals(items.get(0).getLastBooking().getStart().getHour(),
                lastTime.toLocalDateTime().getHour());
        assertEquals(items.get(0).getNextBooking().getStart().getHour(),
                nextTime.toLocalDateTime().getHour());
    }

    @Test
    public void getAllItemsOfOwnerWithCurrentTest() {
        long bookerId = 5L;
        long ownerId = 4L;
        long itemId = 7;
        long bookingId;
        int from = 0;
        int size = 20;
        //создаем бронь с нужными диапазонами и одобрениями
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-5L), TestUtils.fromOffset(-4L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(-3L), TestUtils.fromOffset(-2L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        Timestamp lastTime = bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(-1L), TestUtils.fromOffset(3L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 4L, 5L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        Timestamp nextTime = bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(4L), TestUtils.fromOffset(5L));
        //читаем список вещей владельца
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, from, size);
        assertEquals(items.size(), 4);
        assertEquals(items.get(0).getLastBooking().getStart().getHour(),
                lastTime.toLocalDateTime().getHour());
        assertEquals(items.get(0).getNextBooking().getStart().getHour(),
                nextTime.toLocalDateTime().getHour());
    }

    @Test
    public void getAllItemsOfOwnerWithoutNextTest() {
        long bookerId = 5L;
        long ownerId = 4L;
        long itemId = 7;
        long bookingId;
        int from = 0;
        int size = 20;
        //создаем бронь с нужными диапазонами и одобрениями
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        bookingService.updateBounds(bookingId, TestUtils.fromOffset(-5L), TestUtils.fromOffset(-4L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        Timestamp lastTime = bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(-3L), TestUtils.fromOffset(-1L));
        //читаем список вещей владельца
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, from, size);
        assertEquals(items.size(), 4);
        assertEquals(items.get(0).getLastBooking().getStart().getHour(),
                lastTime.toLocalDateTime().getHour());
        assertNull(items.get(0).getNextBooking());
    }

    @Test
    public void getAllItemsOfOwnerWithoutLastTest() {
        long bookerId = 5L;
        long ownerId = 4L;
        long itemId = 7;
        long bookingId;
        int from = 0;
        int size = 20;
        //создаем бронь с нужными диапазонами и одобрениями
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        Timestamp lastTime = bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(1L), TestUtils.fromOffset(3L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 4L, 6L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        bookingService.updateBounds(bookingId,
                TestUtils.fromOffset(4L), TestUtils.fromOffset(6L));
        //читаем список вещей владельца
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, from, size);
        assertEquals(items.size(), 4);
        assertNull(items.get(0).getLastBooking());
        assertEquals(items.get(0).getNextBooking().getStart().getHour(),
                lastTime.toLocalDateTime().getHour());
    }
}
