package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.sql.Timestamp;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class BookingDto {
    private long id;
    private Timestamp start;
    private Timestamp end;
    private Long itemId;
    private Long bookerId;
    private Integer status;
}
