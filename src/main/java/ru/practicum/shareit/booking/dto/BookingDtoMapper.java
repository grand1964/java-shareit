package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.dto.UserOutDto;

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
        UserOutDto bookerOutDto = UserDtoMapper.toUserOutDto(booking.getBooker());
        ItemOutDto itemOutDto = ItemDtoMapper.toItemOutDto(booking.getItem());
        return new BookingOutDto(booking.getId(), start, end, booking.getStatus(),
                bookerOutDto, itemOutDto,
                booking.getBooker().getId(), booking.getItem().getId());
    }

    public static Booking toBooking(BookingInDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(null);
        booking.setStart(Timestamp.valueOf(bookingDto.getStart().toLocalDateTime().minusHours(3)));
        booking.setEnd(Timestamp.valueOf(bookingDto.getEnd().toLocalDateTime().minusHours(3)));
        return booking;
    }

    public static List<BookingOutDto> listToBookingDto(List<Booking> bookings) {
        return bookings.stream().map(BookingDtoMapper::toBookingDto).collect(Collectors.toList());
    }
}
