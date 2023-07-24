package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

public class TestUtils {

    public static ItemDto createItemDto(String name, String description, Boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }
}
