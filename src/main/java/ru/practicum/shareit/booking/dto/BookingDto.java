package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.sql.Timestamp;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class BookingDto {
    long id;
    Timestamp start;
    Timestamp end;
    Long itemId;
    Long bookerId;
    Integer status;
}
