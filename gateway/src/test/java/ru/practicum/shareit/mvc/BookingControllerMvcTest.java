package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final String HEADER_NAME = "X-Sharer-User-Id";
    private static final long itemId = 1L;
    private static final long bookingId = 1L;
    private static BookingInDto bookingInDto;
    private static BookingOutDto bookingOutDto;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingClient bookingClient;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        Instant now = Instant.now();
        //создаем входное бронирование
        bookingInDto = new BookingInDto(itemId, Timestamp.from(now.plusSeconds(3600)),
                Timestamp.from(now.plusSeconds(7200)));
        //создаем выходное бронирование
        bookingOutDto = new BookingOutDto(itemId, bookingInDto.getStart().toLocalDateTime(),
                bookingInDto.getEnd().toLocalDateTime());
    }

    ///////////////////////////// Получение данных ///////////////////////////

    @Test
    void normalGetBookingTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(makeResponse(bookingOutDto));

        mvc.perform(get("/bookings/{id}", bookingId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$.end", is(convertTime(bookingOutDto.getEnd()))));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void getBookingWithNonPositiveBookingTest(long value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/bookings/{id}", value)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void getBookingWithNonPositiveOwnerTest(String value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, value);
        mvc.perform(get("/bookings/{id}", bookingId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void normalGetAllBookingsTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(bookingClient.getAllBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(bookingOutDto)));

        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$[0].end", is(convertTime(bookingOutDto.getEnd()))));
    }

    @Test
    void getAllBookingsWithBadStateTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/bookings?state={state}&from={from}&size={size}",
                        "UNSUPPORTED", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsWithNegativeFromTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/bookings?state={state}&from={from}&size={size}",
                        "PAST", -1, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsWithNonPositiveSizeTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/bookings?state={state}&from={from}&size={size}",
                        "FUTURE", 0, 0)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mvc.perform(get("/bookings?state={state}&from={from}&size={size}",
                        "WAITING", 0, -1)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void normalGetAllBookingsForOwnerTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(bookingClient.getAllBookingsForOwner(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(bookingOutDto)));

        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}",
                        "ALL", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$[0].end", is(convertTime(bookingOutDto.getEnd()))));
    }

    @Test
    void getAllBookingsForOwnerWithBadStateTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}",
                        "UNSUPPORTED", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsForOwnerWithNegativeFromTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}",
                        "PAST", -1, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsForOwnerWithNonPositiveSizeTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}",
                        "FUTURE", 0, 0)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mvc.perform(get("/bookings?state={state}&from={from}&size={size}",
                        "WAITING", 0, -1)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }


    /////////////////////////// Создание и обновление ////////////////////////

    @Test
    void normalCreateBookingTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(bookingClient.createBooking(anyLong(), any(BookingInDto.class)))
                .thenReturn(makeResponse(bookingOutDto));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(convertTime(bookingOutDto.getStart()))))
                .andExpect(jsonPath("$.end", is(convertTime(bookingOutDto.getEnd()))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void createBookingWithNonPositiveBookerIdTest(String value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, value);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void createBookingWithStartInPastTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        //создаем копию bookingInDto с устаревшим стартом
        BookingInDto badBooking = new BookingInDto(1L,
                Timestamp.from(Instant.now().minusSeconds(3600)), bookingInDto.getEnd());
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(badBooking))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void createBookingWithStartEqualsEndTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        //создаем копию bookingInDto с одинаковыми стартом и финишем
        BookingInDto badBooking = new BookingInDto(1L, bookingInDto.getStart(), bookingInDto.getStart());
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(badBooking))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void createBookingWithStartAfterEndTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        //создаем копию bookingInDto с одинаковыми стартом и финишем
        BookingInDto badBooking = new BookingInDto(1L, bookingInDto.getEnd(), bookingInDto.getStart());
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(badBooking))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void normalConfirmBookingTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        when(bookingClient.confirmBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(makeResponse(bookingOutDto));

        mvc.perform(patch("/bookings/{id}?approved={approved}", bookingId, false)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void confirmBookingWithNotPositiveOwnerIdTest(String value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, value);

        mvc.perform(patch("/bookings/{id}?approved={approved}", bookingId, true)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void confirmBookingWithNotPositiveBookingIdTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(patch("/bookings/{id}?approved={approved}", 0, true)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        headers.add(HEADER_NAME, "-1");
        mvc.perform(patch("/bookings/{id}?approved={approved}", -1, true)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    /////////////////////////// Формирование ответа //////////////////////////

    private ResponseEntity<Object> makeResponse(Object objectInDto) {
        return ResponseEntity.ok(objectInDto);
    }

    private ResponseEntity<Object> makeResponse(List<Object> objects) {
        return ResponseEntity.ok(objects);
    }

    ////////////////////////////// Формат времени ////////////////////////////

    private String convertTime(LocalDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(dateTime);
    }
}
