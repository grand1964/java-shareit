package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
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
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    public BookingOutDto createBooking(Long bookerId, BookingInDto bookingInDto) {
        //проверяем временные характеристики брони
        if (!bookingInDto.validate()) {
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
            throw new NotFoundException("Нельзя забронировать свою вещь.");
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
                    item.getId(), booking.getStart(), booking.getEnd(), Status.APPROVED).isEmpty()) { //есть наложения
                throw new NotFoundException("В данный момент вещь недоступна.");
            }
            booking.setStatus(Status.APPROVED);
            log.info("Бронирование с идентификатором " + bookingId + " согласовано.");
        } else {
            booking.setStatus(Status.REJECTED);
            log.info("Бронирование с идентификатором " + bookingId + " отвергнуто.");
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
    public List<BookingOutDto> getAllBookingsForBooker(Long bookerId, String state, int from, int size) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException("Пользователь " + bookerId + " не найден.");
        }
        PageRequest pageable = PageRequest.of(from / size, size);
        List<BookingOutDto> dtoList;
        switch (state) {
            case "ALL":
                dtoList = bookingRepository
                        .findByBooker_IdOrderByStartDesc(bookerId, pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "PAST":
                dtoList = bookingRepository.findByBooker_IdAndEndIsBeforeOrderByStartDesc(
                                bookerId, Timestamp.from(Instant.now()), pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "FUTURE":
                dtoList = bookingRepository.findByBooker_IdAndStartIsAfterOrderByStartDesc(
                                bookerId, Timestamp.from(Instant.now()), pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "CURRENT":
                Timestamp timestamp = Timestamp.from(Instant.now());
                dtoList = bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                                bookerId, timestamp, timestamp, pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "WAITING":
                dtoList = bookingRepository.findByBooker_IdAndStatusIsOrderByStartDesc(
                                bookerId, Status.WAITING, pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "REJECTED":
                dtoList = bookingRepository.findByBooker_IdAndStatusIsOrderByStartDesc(
                                bookerId, Status.REJECTED, pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return dtoList;
    }

    public List<BookingOutDto> getAllBookingsForOwner(Long ownerId, String state, int from, int size) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Владелец " + ownerId + " не найден.");
        }
        PageRequest pageable = PageRequest.of(from / size, size);
        List<BookingOutDto> dtoList;
        switch (state) {
            case "ALL":
                dtoList = bookingRepository.findAllBookingsForOwner(ownerId, pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "PAST":
                dtoList = bookingRepository
                        .findPastBookingsForOwner(ownerId, Timestamp.from(Instant.now()), pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "FUTURE":
                dtoList = bookingRepository
                        .findFutureBookingsForOwner(ownerId, Timestamp.from(Instant.now()), pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "CURRENT":
                dtoList = bookingRepository
                        .findCurrentBookingsForOwner(ownerId, Timestamp.from(Instant.now()), pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "WAITING":
                dtoList = bookingRepository
                        .findStatusBookingsForOwner(ownerId, Status.WAITING, pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            case "REJECTED":
                dtoList = bookingRepository
                        .findStatusBookingsForOwner(ownerId, Status.REJECTED, pageable)
                        .map(BookingDtoMapper::toBookingDto)
                        .getContent();
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return dtoList;
    }

    //////////////////////////// Поддержка тестов ///////////////////////////

    public Timestamp updateBounds(Long bookingId, Timestamp start, Timestamp end) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new BadRequestException("Бронь с идентификатором " + bookingId + " не найдена.")
        );
        booking.setStart(start);
        booking.setEnd(end);
        bookingRepository.save(booking);
        return start;
    }
}
