package ru.practicum.shareit.integral.booking;

import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.user.dto.UserInDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;

public class TestUtils {

    public static UserInDto createUserInDto(String name, String email) {
        return new UserInDto(name, email);
    }

    public static ItemInDto createItemInDto(String name, String description, Boolean available) {
        return new ItemInDto(null, name, description, available, null);
    }

    public static BookingInDto createBookingInDto(long itemId, Long startOffsetInDays, Long endOffsetInDays) {
        return new BookingInDto(itemId,
                startOffsetInDays == null ? null : fromOffset(startOffsetInDays),
                endOffsetInDays == null ? null : fromOffset(endOffsetInDays));
    }

    public static String getSqlForReset() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DROP TABLE users, items, bookings, comments CASCADE; ");
        try {
            stringBuilder.append(new String(Files.readAllBytes(
                    Path.of("./src/main/resources/schema.sql"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    public static Timestamp fromOffset(Long days) {
        return Timestamp.from(Instant.now().plusSeconds(days * 24 * 3600));
    }
}
