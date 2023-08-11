package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingOutDto {
    private Long id;
    private String start;
    private String end;
    private Status status;
    private User booker;
    private Item item;
    private Long bookerId;
    private Long itemId;
}
