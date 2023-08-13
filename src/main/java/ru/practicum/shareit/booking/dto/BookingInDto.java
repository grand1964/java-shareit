package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInDto {
    private Long itemId;
    @Future
    @NotNull
    private Timestamp start;
    @NotNull
    private Timestamp end;

    public boolean validate() {
        //а это - реальная проверка
        return (end.after(start));
    }
}
