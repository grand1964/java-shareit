package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.sql.Timestamp;
import java.time.Instant;

public class ItemRequestDtoMapper {
    public static ItemRequest toItemRequest(User requester, ItemRequestInDto dto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setCreated(Timestamp.from(Instant.now()));
        itemRequest.setRequester(requester);
        return itemRequest;
    }

    public static ItemRequestOutCreationDto toItemRequestCreationDto(ItemRequest itemRequest) {
        ItemRequestOutCreationDto dto = new ItemRequestOutCreationDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setCreated(itemRequest.getCreated().toLocalDateTime());
        return dto;
    }

    public static ItemRequestOutDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestOutDto dto = new ItemRequestOutDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setCreated(itemRequest.getCreated().toLocalDateTime());
        return dto;
    }
}
