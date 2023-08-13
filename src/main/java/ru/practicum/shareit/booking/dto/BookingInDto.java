package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInDto {
    private Long itemId;
    @NotNull
    private Timestamp start;
    @NotNull
    private Timestamp end;

    public boolean validate() {
        //а это - реальная проверка
        return (end.after(start) && start.after(Timestamp.from(Instant.now())));
    }
}
