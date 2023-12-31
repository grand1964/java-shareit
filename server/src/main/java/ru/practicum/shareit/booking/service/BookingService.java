package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto createBooking(Long userId, BookingInDto bookingInDto);

    BookingOutDto confirmBooking(Long bookingId, Long userId, Boolean confirm);

    BookingOutDto getBookingById(Long bookingId, Long userId);

    List<BookingOutDto> getAllBookingsForBooker(Long bookerId, String state, Pageable pageable);

    List<BookingOutDto> getAllBookingsForOwner(Long bookerId, String state, Pageable pageable);

    //нужен для тестов
    //Timestamp updateBounds(Long bookingId, Timestamp start, Timestamp end);
}
