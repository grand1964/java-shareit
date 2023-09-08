package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class BookingController {
    private static final String HEADER_NAME = "X-Sharer-User-Id";
    private final BookingService service;

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех бронирований пользователя
    @GetMapping
    public List<BookingOutDto> getAllBookings(@RequestHeader(HEADER_NAME) Long bookerId,
                                              @RequestParam(defaultValue = "ALL") String state,
                                              @RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "20") int size) {
        log.info("Запрошено получение всех бронирований пользователя с идентификатором " + bookerId);
        PageRequest pageable = PageRequest.of(from / size, size);
        return service.getAllBookingsForBooker(bookerId, state, pageable);
    }

    //получение всех бронирований всех вещей одного владельца
    @GetMapping(value = "/owner")
    public List<BookingOutDto> getAllOwnerBookings(@RequestHeader(HEADER_NAME) Long ownerId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "20") int size) {
        log.info("Запрошено получение бронирований вещей пользователя с идентификатором " + ownerId);
        PageRequest pageable = PageRequest.of(from / size, size);
        return service.getAllBookingsForOwner(ownerId, state, pageable);
    }

    //получение бронирования по его идентификатору
    @GetMapping(value = "/{id}")
    public BookingOutDto getBooking(@PathVariable("id") long bookingId, @RequestHeader(HEADER_NAME) Long ownerId) {
        log.info("Запрошены данные бронирования с идентификатором " + bookingId);
        return service.getBookingById(bookingId, ownerId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание новой брони
    @PostMapping
    public BookingOutDto createBooking(@RequestHeader(HEADER_NAME) Long bookerId,
                                       @RequestBody BookingInDto bookingInDto) {
        log.info("Запрошено создание новой брони пользователем с идентификатором " + bookerId);
        return service.createBooking(bookerId, bookingInDto);
    }

    //подтверждение запрошенной ранее брони владельцем
    @PatchMapping(value = "/{id}")
    public BookingOutDto confirmBooking(@PathVariable("id") long bookingId,
                                        @RequestHeader(HEADER_NAME) Long ownerId,
                                        @RequestParam Boolean approved) {
        log.info("Владелец " + ownerId + " согласовывает бронь с идентификатором " + bookingId);
        return service.confirmBooking(bookingId, ownerId, approved);
    }
}
