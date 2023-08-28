package ru.practicum.shareit.integral.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemTests {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final CommentService commentService;
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

    ///////////////////////////// Тест создания //////////////////////////////

    @Test
    public void createItemTest() {
        long ownerId = 1L;
        int from = 0;
        int size = 20;
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("drill", "Makita", null));
        PageRequest pageable = PageRequest.of(from / size, size);
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, pageable);
        assertEquals(items.size(), 2);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(0).getAvailable(), true);
        assertEquals(items.get(1).getDescription(), "Makita");
        assertNull(items.get(1).getAvailable());
    }

    ////////////////////////////// Тесты чтения //////////////////////////////

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
        PageRequest pageable = PageRequest.of(from / size, size);
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, pageable);
        assertEquals(items.size(), 3);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(1).getDescription(), "Samsung");
        assertFalse(items.get(2).getAvailable());
    }

    @Test
    public void getAllItemsWithPastAndCurrentBookingTest() {
        long ownerId = 1L;
        long itemId = 1L;
        long bookerId = 2L;
        long bookingId;
        int from = 0;
        int size = 20;
        //фиксируем текущее время
        Timestamp now = Timestamp.from(Instant.now());
        //создаем вещь одного владельца
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        //создаем бронь одного заказчика с нужными диапазонами и одобрениями
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId,
                TestUtils.offsetTime(now,-2L), TestUtils.offsetTime(now, -1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId,
                TestUtils.offsetTime(now,-1L), TestUtils.offsetTime(now, 1L));
        //создаем комментарий от заказчика
        commentService.createComment(itemId, bookingId, TestUtils.createCommentInDto("Good item"));
        //получаем список владельца 1
        PageRequest pageable = PageRequest.of(from / size, size);
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, pageable);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(0).getDescription(), "ASUS");
        assertTrue(items.get(0).getAvailable());
        assertEquals(items.get(0).getLastBooking().getStart(),
                TestUtils.offsetTime(now, -1L).toLocalDateTime());
        assertNull(items.get(0).getNextBooking());
        assertEquals(items.get(0).getComments().size(), 1);
        assertEquals(items.get(0).getComments().get(0).getText(), "Good item");
    }

    @Test
    public void getAllItemsWithPastAndFutureBookingTest() {
        long ownerId = 1L;
        long itemId = 1L;
        long bookerId = 2L;
        long bookingId;
        int from = 0;
        int size = 20;
        //фиксируем текущее время
        Timestamp now = Timestamp.from(Instant.now());
        //создаем вещь одного владельца
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        //создаем бронь одного заказчика с нужными диапазонами и одобрениями
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 3L, 4L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId,
                TestUtils.offsetTime(now,-2L), TestUtils.offsetTime(now, -1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 3L, 4L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId,
                TestUtils.offsetTime(now,1L), TestUtils.offsetTime(now, 2L));
        //создаем два комментария от заказчика
        commentService.createComment(itemId, bookingId, TestUtils.createCommentInDto("Good item"));
        commentService.createComment(itemId, bookingId, TestUtils.createCommentInDto("Very good"));
        //получаем список владельца 1
        PageRequest pageable = PageRequest.of(from / size, size);
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, pageable);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(0).getDescription(), "ASUS");
        assertTrue(items.get(0).getAvailable());
        assertEquals(items.get(0).getLastBooking().getStart(),
                TestUtils.offsetTime(now, -2L).toLocalDateTime());
        assertEquals(items.get(0).getNextBooking().getStart(),
                TestUtils.offsetTime(now, 1L).toLocalDateTime());
        assertEquals(items.get(0).getComments().size(), 2);
    }

    @Test
    public void getItemsWithPastAndFutureBookingTest() {
        long ownerId = 1L;
        long itemId = 1L;
        long bookerId = 2L;
        long bookingId;
        //фиксируем текущее время
        Timestamp now = Timestamp.from(Instant.now());
        //создаем вещь одного владельца
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        //создаем бронь одного заказчика с нужными диапазонами и одобрениями
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 3L, 4L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId,
                TestUtils.offsetTime(now,-2L), TestUtils.offsetTime(now, -1L));
        bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 3L, 4L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId,
                TestUtils.offsetTime(now,1L), TestUtils.offsetTime(now, 2L));
        //создаем два комментария от заказчика
        commentService.createComment(itemId, bookingId, TestUtils.createCommentInDto("Good item"));
        commentService.createComment(itemId, bookingId, TestUtils.createCommentInDto("Very good"));
        //получаем список владельца 1
        ItemOutBookedDto item = itemService.getItem(itemId, ownerId);
        assertEquals(item.getName(), "notebook");
        assertEquals(item.getDescription(), "ASUS");
        assertTrue(item.getAvailable());
        assertEquals(item.getLastBooking().getStart(),
                TestUtils.offsetTime(now, -2L).toLocalDateTime());
        assertEquals(item.getNextBooking().getStart(),
                TestUtils.offsetTime(now, 1L).toLocalDateTime());
        assertEquals(item.getComments().size(), 2);
        assertEquals(item.getComments().get(0).getText(), "Good item");
        assertEquals(item.getComments().get(1).getText(), "Very good");
    }

    ///////////////////////////// Тесты удаления /////////////////////////////

    @Test
    public void deleteItemTest() {
        long ownerId = 1L;
        long itemId = 1L;
        //создаем вещь одного владельца
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        assertEquals(itemService.getItem(itemId, ownerId).getId(), itemId);
        itemService.deleteItem(itemId);
        assertThrows(NotFoundException.class, () -> itemService.getItem(itemId, ownerId));
    }

    @Test
    public void deleteAllItemsTest() {
        long ownerId = 1L;
        //создаем две вещи одного владельца
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("smartphone", "Samsung", true));
        itemService.deleteAllItems();
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, PageRequest.of(0, 20));
        assertEquals(items.size(), 0);
    }

    ////////////////////////////// Тесты поиска //////////////////////////////

    @Test
    public void searchWithEmptySampleTest() {
        long ownerId = 1L;
        //создаем вещь одного владельца
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        List<ItemOutDto> searched = itemService.searchItems("", PageRequest.of(0, 20));
        assertEquals(searched.size(), 0);
    }

    @Test
    public void normalSearchTest() {
        long ownerId = 1L;
        //создаем вещь одного владельца
        itemService.createItem(ownerId,
                TestUtils.createItemInDto("notebook", "ASUS", true));
        List<ItemOutDto> searched = itemService.searchItems("book", PageRequest.of(0, 20));
        assertEquals(searched.size(), 1);
        assertEquals(searched.get(0).getId(), 1);
        searched = itemService.searchItems("su", PageRequest.of(0, 20));
        assertEquals(searched.size(), 1);
        assertEquals(searched.get(0).getDescription(), "ASUS");
    }

    /////////////////////// Коррекция дат бронирований //////////////////////

    private Timestamp updateBounds(Long bookingId, Timestamp start, Timestamp end) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new BadRequestException("Бронь с идентификатором " + bookingId + " не найдена.")
        );
        booking.setStart(start);
        booking.setEnd(end);
        bookingRepository.save(booking);
        return start;
    }
}
