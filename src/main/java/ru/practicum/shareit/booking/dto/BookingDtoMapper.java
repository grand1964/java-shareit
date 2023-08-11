package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class BookingDtoMapper {
    public static BookingOutDto toBookingDto(Booking booking) {
        String start;
        String end;
        if (booking.getStart() == null) {
            start = "";
        } else {
            start = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(booking.getStart());
        }
        if (booking.getStart() == null) {
            end = "";
        } else {
            end = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(booking.getEnd());
        }
        return new BookingOutDto(booking.getId(), start, end, booking.getStatus(),
                booking.getBooker(), booking.getItem(),
                booking.getBooker().getId(), booking.getItem().getId());
    }

    public static Booking toBooking(BookingInDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(null);
        booking.setStart(Timestamp.valueOf(bookingDto.getStart().toLocalDateTime()));
        booking.setEnd(Timestamp.valueOf(bookingDto.getEnd().toLocalDateTime()));
        return booking;
    }

    public static List<BookingOutDto> listToBookingDto(List<Booking> bookings) {
        return bookings.stream().map(BookingDtoMapper::toBookingDto).collect(Collectors.toList());
    }
}
