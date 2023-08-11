package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInDto {
    private Long itemId;
    private Timestamp start;
    private Timestamp end;

    public boolean validate() {
        //страховка для тестов
        if ((start == null) || (end == null)) {
            return false;
        }
        //а это - реальная проверка
        return (end.after(start) && start.after(Timestamp.from(Instant.now())));
    }
}
