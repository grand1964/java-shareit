package ru.practicum.shareit.common.validation;

import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserInDto;

public class Validation {

    ///////////////////////// Валидация пользователя /////////////////////////

    //исправление некорректного email
    public static void validateUserDto(UserInDto userInDto) {
        if (!validateEmail(userInDto.getEmail())) {
            userInDto.setEmail(""); //некорректные адреса заменяем пустыми
        }
    }

    //проверка корректности email
    private static boolean validateEmail(String email) {
        if ((email == null) || (email.isBlank())) {
            return false; //адрес должен быть непустым
        }
        int pos = email.indexOf('@');
        if ((pos < 1) || (pos == email.length() - 1)) {
            return false; //@ должно быть не первым и не последним символом
        }
        return (email.indexOf(' ') == -1); //не должно быть пробелов
    }

    ////////////////////////////// Валидация брони ////////////////////////////

    public static void validateBookingDto(BookingInDto bookingInDto) {
        if (!bookingInDto.getEnd().after(bookingInDto.getStart())) {
            throw new BadRequestException("Неверные границы бронирования");
        }
    }

    ///////////////////////// Валидация идентификаторов ///////////////////////

    public static void validateId(long... ids) {
        for (long id : ids) {
            if (id <= 0) {
                throw new NotFoundException("Идентификатор должен быть положительным");
            }
        }

    }
}
