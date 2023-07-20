package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemDtoMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static Item toItem(Long ownerId, ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(ownerId)
                .build();
    }

    public static List<ItemDto> listToItemDto(List<Item> list) {
        return list.stream().map(ItemDtoMapper::toItemDto).collect(Collectors.toList());
    }
}
