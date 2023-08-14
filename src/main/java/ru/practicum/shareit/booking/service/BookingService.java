package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto createBooking(Long userId, BookingInDto bookingInDto);

    BookingOutDto confirmBooking(Long bookingId, Long userId, Boolean confirm);

    BookingOutDto getBookingById(Long bookingId, Long userId);

    List<BookingOutDto> getAllBookingsForBooker(Long bookerId, String state);

    List<BookingOutDto> getAllBookingsForOwner(Long bookerId, String state);
}
