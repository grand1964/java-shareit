package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;

import java.sql.Timestamp;

public class BookingDtoMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .start(Timestamp.valueOf(booking.getStart()))
                .end(Timestamp.valueOf(booking.getEnd()))
                .itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .status(booking.getStatus().ordinal())
                .build();
    }
}
