package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto getItem(long ownerId);

    List<ItemDto> getAllItems(Long ownerId);

    ItemDto createItem(Long ownerId, ItemDto itemDto);

    ItemDto patchItem(Long ownerId, ItemDto itemDto);

    void deleteItem(long id);

    void deleteAllItems();

    List<ItemDto> searchItems(String sample);
}
