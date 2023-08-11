package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingOutDto createBooking(Long bookerId, BookingInDto bookingInDto) {
        //проверяем временные характеристики брони
        if (!bookingInDto.Validate()) {
            throw new BadRequestException("Некорректный диапазон бронирования.");
        }
        long itemId = bookingInDto.getItemId(); //идентификатор вещи
        //читаем вещь и проверяем ее существование
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с идентификатором " + itemId + " не найдена."));
        //проверяем доступность вещи
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь с идентификатором " + itemId + " недоступна.");
        }
        //проверяем, что заказчик не совпадает с хозяином
        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Нельзя оформить заказ на свою вещь.");
        }
        //читаем заказчика и проверяем его существование
        User booker = userRepository.findById(bookerId).orElseThrow(
                () -> new NotFoundException("Пользователь с идентификатором " + bookerId + " не найден.")
        );
        //заполняем запрос
        Booking booking = BookingDtoMapper.toBooking(bookingInDto);
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);
        //создаем запрос в базе
        log.info("Создан запрос на бронирование с идентификатором " + item.getId());
        return BookingDtoMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto confirmBooking(Long bookingId, Long userId, Boolean confirm) {
        //читаем бронь и проверяем корректность идентификатора
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BadRequestException("Бронь с идентификатором " + bookingId + " не найдена."));
        Item item = booking.getItem();
        //проверяем права подтверждения (странно это сделано в Postman)
        if (booking.getBooker().getId().equals(userId)) { //подтверждает заказчик
            throw new NotFoundException("Бронь должен подтвердить владелец.");
        }
        if (!item.getOwner().getId().equals(userId)) { //подтверждает не владелец
            throw new BadRequestException("Согласовывать бронь может только владелец.");
        }
        //владелец может подтверждать только новую бронь
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Согласовывать можно только новую бронь.");
        }
        //если все проверки прошли - подтверждаем
        if (confirm) {
            //проверяем наложения новой брони на старые
            if (!bookingRepository.findByItem_IdAndEndAfterAndStartBeforeAndStatusIs(
                    userId, booking.getStart(), booking.getEnd(), Status.APPROVED).isEmpty()) { //есть наложения
                throw new NotFoundException("В данный момент вещь недоступна.");
            }
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingDtoMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Бронь с идентификатором " + bookingId + "не найдена.")
        );
        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Пользователю " + userId + " эта информация недоступна.");
        }
        return BookingDtoMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingOutDto> getAllBookingsForBooker(Long bookerId, String state) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException("Пользователь " + bookerId + " не найден.");
        }
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository
                        .findByBooker_IdOrderByStartDesc(bookerId);
                break;
            case "PAST":
                bookings = bookingRepository.findByBooker_IdAndEndIsBeforeOrderByStartDesc(
                        bookerId, Timestamp.from(Instant.now()));
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBooker_IdAndStartIsAfterOrderByStartDesc(
                        bookerId, Timestamp.from(Instant.now()));
                break;
            case "CURRENT":
                Timestamp timestamp = Timestamp.from(Instant.now());
                bookings = bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                        bookerId, timestamp, timestamp);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBooker_IdAndStatusIsOrderByStartDesc(
                        bookerId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBooker_IdAndStatusIsOrderByStartDesc(
                        bookerId, Status.REJECTED);
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return BookingDtoMapper.listToBookingDto(bookings);
    }

    public List<BookingOutDto> getAllBookingsForOwner(Long ownerId, String state) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Владелец " + ownerId + " не найден.");
        }
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findAllBookingsForOwner(ownerId);
                break;
            case "PAST":
                bookings = bookingRepository.findPastBookingsForOwner(ownerId, Timestamp.from(Instant.now()));
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureBookingsForOwner(ownerId, Timestamp.from(Instant.now()));
                break;
            case "CURRENT":
                bookings = bookingRepository.findCurrentBookingsForOwner(ownerId, Timestamp.from(Instant.now()));
                break;
            case "WAITING":
                bookings = bookingRepository.findStatusBookingsForOwner(ownerId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findStatusBookingsForOwner(ownerId, Status.REJECTED);
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return BookingDtoMapper.listToBookingDto(bookings);
    }
}
