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
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.integral.item.TestUtils.createItemInDto;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentTests {
    private static final int USER_COUNT = 10;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final CommentService commentService;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        //создаем пользователей
        for (int i = 1; i <= USER_COUNT; i++) {
            userService.createUser(TestUtils.createUserInDto(
                    "user" + i, "user" + i + "@com"));
        }
        //создаем вещи
        itemService.createItem(1L, createItemInDto(
                "item1", "", false));
        int ind = 1;
        for (int i = 2; i <= 4; i++) {
            for (int j = 1; j <= i; j++) {
                ind++;
                itemService.createItem((long) i, createItemInDto("item" + ind, "", true));
            }
        }
    }

    @AfterEach
    public void resetData() {
        jdbcTemplate.update(TestUtils.getSqlForReset());
    }

    ///////////////////////////// Тесты создания /////////////////////////////

    @Test
    public void createNormalCommentTest() {
        long itemId = 2L;
        long ownerId = 2L;
        long bookerId = 5L;
        long bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId, TestUtils.fromOffset(-2L), TestUtils.fromOffset(-1L));
        commentService.createComment(itemId, bookerId, TestUtils.createCommentInDto("Комментарий"));
        ItemOutBookedDto itemDto = itemService.getItem(itemId, ownerId);
        List<CommentOutDto> comments = itemDto.getComments();
        assertEquals(comments.size(), 1);
        assertEquals(comments.get(0).getText(), "Комментарий");
        assertEquals(comments.get(0).getAuthorName(), "user5");
    }


    ////////////////////////////// Тесты чтения //////////////////////////////

    @Test
    public void getCommentsToSingleItemTest() {
        long itemId = 2;
        long ownerId = 2;
        //создаем подтвержденные аренды и комментарии
        Long bookingId = bookingService.createBooking(5L,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId, TestUtils.fromOffset(-2L), TestUtils.fromOffset(-1L));
        commentService.createComment(itemId, 5L, TestUtils.createCommentInDto("Item 2 is good"));
        Long bookingId1 = bookingService.createBooking(6L,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId1, ownerId, true);
        updateBounds(bookingId1, TestUtils.fromOffset(-4L), TestUtils.fromOffset(-3L));
        commentService.createComment(itemId, 5L, TestUtils.createCommentInDto("Item 2 is bad"));
        Long bookingId2 = bookingService.createBooking(7L,
                TestUtils.createBookingInDto(itemId, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId2, ownerId, true);
        updateBounds(bookingId2, TestUtils.fromOffset(-6L), TestUtils.fromOffset(-5L));
        commentService.createComment(itemId, 5L, TestUtils.createCommentInDto("Item 2 is normal"));
        commentService.createComment(itemId, 5L, TestUtils.createCommentInDto("Again to item 2"));
        ItemOutBookedDto itemDto = itemService.getItem(itemId, ownerId);
        List<CommentOutDto> comments = itemDto.getComments();
        assertEquals(comments.size(), 4);
        assertEquals(comments.get(0).getText(), "Item 2 is good");
        assertEquals(comments.get(1).getText(), "Item 2 is bad");
        assertEquals(comments.get(2).getText(), "Item 2 is normal");
        assertEquals(comments.get(3).getText(), "Again to item 2");
    }

    @Test
    public void getCommentsToAllItemOfSingleOwnerTest() {
        long bookerId = 5;
        long ownerId = 3;
        int from = 0;
        int size = 20;
        //создаем подтвержденные аренды и комментарии
        Long bookingId = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(4L, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId, ownerId, true);
        updateBounds(bookingId, TestUtils.fromOffset(-2L), TestUtils.fromOffset(-1L));
        commentService.createComment(4L, bookerId, TestUtils.createCommentInDto("Item 4 is good"));
        commentService.createComment(4L, bookerId, TestUtils.createCommentInDto("Thank for item 4"));
        Long bookingId1 = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(5L, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId1, ownerId, true);
        updateBounds(bookingId1, TestUtils.fromOffset(-4L), TestUtils.fromOffset(-3L));
        commentService.createComment(5L, bookerId, TestUtils.createCommentInDto("Item 5 is bad"));
        commentService.createComment(5L, bookerId, TestUtils.createCommentInDto("No recommend item 5"));
        Long bookingId2 = bookingService.createBooking(bookerId,
                TestUtils.createBookingInDto(6L, 1L, 2L)).getId();
        bookingService.confirmBooking(bookingId2, ownerId, true);
        updateBounds(bookingId2, TestUtils.fromOffset(-6L), TestUtils.fromOffset(-5L));
        commentService.createComment(6L, bookerId, TestUtils.createCommentInDto("Item 2 is normal"));
        commentService.createComment(6L, bookerId, TestUtils.createCommentInDto("Recommend item 2"));
        PageRequest pageable = PageRequest.of(from / size, size);
        List<ItemOutBookedDto> items = itemService.getAllItems(ownerId, pageable);
        for (ItemOutBookedDto itemDto : items) {
            List<CommentOutDto> comments = itemDto.getComments();
            assertEquals(comments.size(), 2);
        }
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
