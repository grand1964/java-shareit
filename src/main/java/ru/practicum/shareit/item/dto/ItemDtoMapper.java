package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDtoMapper {
    public static ItemOutDto toItemOutDto(Item item) {
        return new ItemOutDto(
                item.getId(), item.getName(), item.getDescription(), item.getAvailable(), new ArrayList<>());
    }

    public static ItemBookedDto toItemBookedDto(Item item) {
        return new ItemBookedDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(),
                null, null, new ArrayList<>());
    }

    public static Item toItem(User owner, ItemInDto itemInDto) {
        Item item = new Item();
        item.setId(itemInDto.getId());
        item.setName(itemInDto.getName());
        item.setDescription(itemInDto.getDescription());
        item.setAvailable(itemInDto.getAvailable());
        item.setOwner(owner);
        return item;
    }

    public static List<ItemOutDto> listToItemDto(List<Item> list) {
        return list.stream().map(ItemDtoMapper::toItemOutDto).collect(Collectors.toList());
    }
}
