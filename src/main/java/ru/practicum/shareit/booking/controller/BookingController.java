package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
                                              @RequestParam(defaultValue = "ALL") String state) {
        log.info("Запрошено получение всех бронирований пользователя с идентификатором " + bookerId);
        return service.getAllBookingsForBooker(bookerId, state);
    }

    //получение всех бронирований всех вещей одного владельца
    @GetMapping(value = "/owner")
    public List<BookingOutDto> getAllOwnerBookings(@RequestHeader(HEADER_NAME) Long ownerId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        log.info("Запрошено получение бронирований вещей пользователя с идентификатором " + ownerId);
        return service.getAllBookingsForOwner(ownerId, state);
    }

    //получение бронирования по его идентификатору
    @GetMapping(value = "/{id}")
    public BookingOutDto getItem(@PathVariable("id") long bookingId, @RequestHeader(HEADER_NAME) Long ownerId) {
        log.info("Запрошены данные бронирования с идентификатором " + bookingId);
        return service.getBookingById(bookingId, ownerId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание новой брони
    @PostMapping
    public BookingOutDto createItem(@RequestHeader(HEADER_NAME) Long bookerId,
                                    @Valid @RequestBody BookingInDto bookingInDto) {
        log.info("Запрошено создание новой брони пользователем с идентификатором " + bookerId);
        return service.createBooking(bookerId, bookingInDto);
    }

    //подтверждение запрошенной ранее брони владельцем
    @PatchMapping(value = "/{id}")
    public BookingOutDto patchItem(@PathVariable("id") long bookingId,
                                   @Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId,
                                   @Valid @NotNull @RequestParam Boolean approved) {
        log.info("Владелец " + ownerId + " согласовывает бронь с идентификатором " + bookingId);
        return service.confirmBooking(bookingId, ownerId, approved);
    }
}
