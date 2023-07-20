package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.ItemRequest;

import java.sql.Timestamp;

public class ItemRequestDtoMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestor() != null ? itemRequest.getRequestor().getId() : null)
                .created(Timestamp.valueOf(itemRequest.getCreated()))
                .build();
    }
}
