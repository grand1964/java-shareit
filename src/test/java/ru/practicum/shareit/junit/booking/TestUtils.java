package ru.practicum.shareit.junit.booking;

import ru.practicum.shareit.booking.dto.BookingInDto;

import java.sql.Timestamp;
import java.time.Instant;

public class TestUtils {
    public static BookingInDto createBookingInDto(long itemId, Long startOffsetInDays, Long endOffsetInDays) {
        return new BookingInDto(itemId,
                startOffsetInDays == null ? null : fromOffset(startOffsetInDays),
                endOffsetInDays == null ? null : fromOffset(endOffsetInDays));
    }

    public static Timestamp fromOffset(Long days) {
        return Timestamp.from(Instant.now().plusSeconds(days * 24 * 3600));
    }
}
