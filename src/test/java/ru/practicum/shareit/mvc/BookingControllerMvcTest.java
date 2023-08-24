package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.user.dto.UserOutDto;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerMvcTest {
    private static HttpHeaders headers;
    private static final long itemId = 1L;
    private static final long userId = 1L;
    private static final long bookingId = 1L;
    private static final long requestId = 1L;
    private static BookingInDto bookingInDto;
    private static BookingOutDto bookingOutDto;
    private static ItemOutDto itemOutDto;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        //создаем дополнительный заголовок
        headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", "1");
        //создаем пользователя
        UserOutDto booker = new UserOutDto(userId, "Vasya", "vasya@com");
        //создаем выходной объект-вещь
        itemOutDto = new ItemOutDto(
                itemId, "notebook", "ASUS", true, requestId, new ArrayList<>());
        //создаем входное бронирование
        bookingInDto = new BookingInDto(itemId, Timestamp.from(Instant.now().plusSeconds(3600)),
                Timestamp.from(Instant.now().plusSeconds(7200)));
        //создаем выходное бронирование
        bookingOutDto = new BookingOutDto(itemId, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), Status.APPROVED, booker, itemOutDto, userId, itemId);
    }

    ///////////////////////////// Получение данных ///////////////////////////

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingOutDto);

        mvc.perform(get("/bookings/{id}", bookingId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$.end", is(convertTime(bookingOutDto.getEnd()))))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(userId), Long.class))
                .andExpect(jsonPath("$.booker.name", is("Vasya")))
                .andExpect(jsonPath("$.booker.email", is("vasya@com")))
                .andExpect(jsonPath("$.item.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.item.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$.item.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.item.requestId", is(itemOutDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(userId), Long.class))
                .andExpect(jsonPath("$.itemId", is(itemId), Long.class));
    }

    @Test
    void getAllBookingsTest() throws Exception {
        when(bookingService.getAllBookingsForBooker(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$[0].end", is(convertTime(bookingOutDto.getEnd()))))
                .andExpect(jsonPath("$[0].status", is(bookingOutDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(userId), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is("Vasya")))
                .andExpect(jsonPath("$[0].booker.email", is("vasya@com")))
                .andExpect(jsonPath("$[0].item.id", is(itemId), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$[0].item.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$[0].item.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].item.requestId", is(itemOutDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(userId), Long.class))
                .andExpect(jsonPath("$[0].itemId", is(itemId), Long.class));
    }

    @Test
    void getAllOwnerBookingsTest() throws Exception {
        when(bookingService.getAllBookingsForOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}",
                        "ALL", 1, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$[0].end", is(convertTime(bookingOutDto.getEnd()))))
                .andExpect(jsonPath("$[0].status", is(bookingOutDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(userId), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is("Vasya")))
                .andExpect(jsonPath("$[0].booker.email", is("vasya@com")))
                .andExpect(jsonPath("$[0].item.id", is(itemId), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$[0].item.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$[0].item.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].item.requestId", is(itemOutDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(userId), Long.class))
                .andExpect(jsonPath("$[0].itemId", is(itemId), Long.class));
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @Test
    void createBookingTest() throws Exception {
        when(bookingService.createBooking(anyLong(), any(BookingInDto.class)))
                .thenReturn(bookingOutDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$.end", is(convertTime(bookingOutDto.getEnd()))))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(userId), Long.class))
                .andExpect(jsonPath("$.booker.name", is("Vasya")))
                .andExpect(jsonPath("$.booker.email", is("vasya@com")))
                .andExpect(jsonPath("$.item.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.item.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$.item.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.item.requestId", is(itemOutDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(userId), Long.class))
                .andExpect(jsonPath("$.itemId", is(itemId), Long.class));
    }

    @Test
    void confirmBookingTest() throws Exception {
        bookingOutDto.setStatus(Status.REJECTED);
        when(bookingService.confirmBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingOutDto);

        mvc.perform(patch("/bookings/{id}?approved={approved}", bookingId, false)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.REJECTED.toString())));
    }

    ///////////////////// Тесты корректности параметров //////////////////////

    @Test
    void getAllBookingsWithBadFromParameterTest() throws Exception {
        when(bookingService.getAllBookingsForBooker(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", -1, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllOwnerBookingsWithBadSizeParameterTest() throws Exception {
        when(bookingService.getAllBookingsForOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}",
                        "ALL", 1, 0)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void createBookingWithNullTimeTest() throws Exception {
        BookingInDto badInDto = new BookingInDto(itemId, null,
                Timestamp.from(Instant.now().plusSeconds(7200)));
        when(bookingService.createBooking(anyLong(), any(BookingInDto.class)))
                .thenReturn(bookingOutDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(badInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void createBookingWithStartInPastTimeTest() throws Exception {
        BookingInDto badInDto = new BookingInDto(itemId,
                Timestamp.from(Instant.now().minusSeconds(3600)),
                Timestamp.from(Instant.now().plusSeconds(7200)));
        when(bookingService.createBooking(anyLong(), any(BookingInDto.class)))
                .thenReturn(bookingOutDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(badInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    ////////////////////////////// Формат времени ////////////////////////////

    private String convertTime(LocalDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(dateTime);
    }
}
