package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemBookedDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingOutDto lastBooking;
    private BookingOutDto nextBooking;
    private List<CommentOutDto> comments;
}
