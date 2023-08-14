package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemBookedDto;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutDto;

import java.util.List;

public interface ItemService {
    ItemBookedDto getItem(long itemId, long userId);

    List<ItemBookedDto> getAllItems(Long ownerId);

    ItemOutDto createItem(Long ownerId, ItemInDto itemInDto);

    ItemBookedDto patchItem(Long ownerId, ItemInDto itemInDto);

    void deleteItem(long id);

    void deleteAllItems();

    List<ItemOutDto> searchItems(String sample);
}
