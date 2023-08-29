package ru.practicum.shareit.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.user.dto.UserOutDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingOutDtoTest {
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingOutDto bookingOutDto;

    @Autowired
    private JacksonTester<BookingOutDto> json;

    @BeforeEach
    public void setUp() {
        long itemId = 1L;
        long userId = 1L;
        long requestId = 1L;
        //создаем пользователя
        UserOutDto booker = new UserOutDto(userId, "Vasya", "vasya@com");
        //создаем выходной объект-вещь
        ItemOutDto itemOutDto = new ItemOutDto(
                itemId, "notebook", "ASUS", true, requestId, new ArrayList<>());
        //задаем временные границы бронирования
        start = LocalDateTime.now().plusHours(1);
        end = LocalDateTime.now().plusHours(2);
        //создаем выходное бронирование
        bookingOutDto = new BookingOutDto(itemId, start, end,
                Status.APPROVED, booker, itemOutDto, userId, itemId);
    }

    ///////////////////////// Проверка преобразования ////////////////////////

    @Test
    void testUserDto() throws Exception {
        String startOrigin = convertTime(start);
        String endOrigin = convertTime(end);
        JsonContent<BookingOutDto> result = json.write(bookingOutDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(startOrigin);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(endOrigin);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(Status.APPROVED.toString());
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Vasya");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("vasya@com");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("notebook");
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo("ASUS");
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }

    ////////////////////////////// Формат времени ////////////////////////////

    private String convertTime(LocalDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(dateTime);
    }
}
