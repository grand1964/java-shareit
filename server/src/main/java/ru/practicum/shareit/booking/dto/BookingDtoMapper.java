package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.dto.UserOutDto;

import java.sql.Timestamp;

public class BookingDtoMapper {
    public static BookingOutDto toBookingDto(Booking booking) {
        UserOutDto bookerOutDto = UserDtoMapper.toUserOutDto(booking.getBooker());
        ItemOutDto itemOutDto = ItemDtoMapper.toItemOutDto(booking.getItem());
        return new BookingOutDto(booking.getId(),
                booking.getStart().toLocalDateTime(),
                booking.getEnd().toLocalDateTime(),
                booking.getStatus(), bookerOutDto, itemOutDto,
                booking.getBooker().getId(), booking.getItem().getId());
    }

    public static Booking toBooking(BookingInDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(null);
        booking.setStart(Timestamp.valueOf(bookingDto.getStart().toLocalDateTime()));
        booking.setEnd(Timestamp.valueOf(bookingDto.getEnd().toLocalDateTime()));
        return booking;
    }
}
