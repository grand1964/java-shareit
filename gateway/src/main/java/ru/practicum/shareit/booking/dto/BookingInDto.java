package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInDto {
    private Long itemId;
    @FutureOrPresent
    @NotNull
    private Timestamp start;
    @NotNull
    private Timestamp end;
}
