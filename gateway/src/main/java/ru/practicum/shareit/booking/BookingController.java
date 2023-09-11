package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String HEADER_NAME = "X-Sharer-User-Id";

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех бронирований пользователя
    @GetMapping
    public ResponseEntity<Object> getAllBookings(@RequestHeader(HEADER_NAME) long bookerId,
                                               @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                               @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                               @Positive @RequestParam(name = "size", defaultValue = "20") Integer size) {
        validateId(bookerId);
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new BadRequestException("Unknown state: UNSUPPORTED_STATUS"));
        log.info("Все бронирования с параметрами: " +
                "state {}, userId={}, from={}, size={}", stateParam, bookerId, from, size);
        return bookingClient.getAllBookings(bookerId, state, from, size);
    }

    //получение всех бронирований всех вещей одного владельца
    @GetMapping(value = "/owner")
    public ResponseEntity<Object> getAllOwnerBookings(@RequestHeader(HEADER_NAME) Long ownerId,
                                                      @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                      @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "20") @Positive int size) {
        validateId(ownerId);
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new BadRequestException("Unknown state: UNSUPPORTED_STATUS"));
        log.info("Бронирования всех вещей пользователя {} параметрами: " +
                "state {}, from={}, size={}", ownerId, stateParam, from, size);
        return bookingClient.getAllBookingsForOwner(ownerId, state, from, size);
    }

    //получение бронирования по его идентификатору
    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(HEADER_NAME) long ownerId,
                                             @PathVariable long bookingId) {
        validateId(bookingId, ownerId);
        log.info("Запрошены данные бронирования {} заказчиком {}", bookingId, ownerId);
        return bookingClient.getBooking(ownerId, bookingId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание новой брони
    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(HEADER_NAME) long bookerId,
                                                @RequestBody @Valid BookingInDto bookingInDto) {
        validateId(bookerId);
        //проверяем корректность границ
        if (!bookingInDto.getEnd().after(bookingInDto.getStart())) {
            throw new BadRequestException("Неверные границы бронирования");
        }
        log.info("Запрошено создание новой брони {} пользователем {}", bookingInDto, bookerId);
        return bookingClient.createBooking(bookerId, bookingInDto);
    }

    //подтверждение запрошенной ранее брони владельцем
    @PatchMapping(value = "/{id}")
    public ResponseEntity<Object> confirmBooking(@Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId,
                                                 @PathVariable("id") @Positive long bookingId,
                                                 @Valid @NotNull @RequestParam Boolean approved) {
        validateId(ownerId);
        log.info("Владелец " + ownerId + " согласовывает бронь с идентификатором " + bookingId);
        return bookingClient.confirmBooking(bookingId, ownerId, approved);
    }

    ///////////////////////// Валидация идентификаторов ///////////////////////

    private void validateId(long... ids) {
        for (long id : ids) {
            if (id <= 0) {
                throw new NotFoundException("Идентификатор должен быть положительным");
            }
        }
    }
}
