package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutDto;

import java.util.List;

public interface ItemService {
    ItemOutBookedDto getItem(long itemId, long userId);

    List<ItemOutBookedDto> getAllItems(Long ownerId, int from, int size);

    ItemOutDto createItem(Long ownerId, ItemInDto itemInDto);

    ItemOutDto patchItem(Long ownerId, ItemInDto itemInDto);

    void deleteItem(long id);

    void deleteAllItems();

    List<ItemOutDto> searchItems(String sample, int from, int size);
}
