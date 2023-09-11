package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.ItemOutBookedDto;
import ru.practicum.shareit.item.dto.ItemInDto;
import ru.practicum.shareit.item.dto.ItemOutDto;

import java.util.List;

public interface ItemService {
    ItemOutBookedDto getItem(long itemId, long userId);

    List<ItemOutBookedDto> getAllItems(Long ownerId, Pageable pageable);

    ItemOutDto createItem(Long ownerId, ItemInDto itemInDto);

    ItemOutDto patchItem(Long ownerId, ItemInDto itemInDto);

    void deleteItem(long id);

    void deleteAllItems();

    List<ItemOutDto> searchItems(String sample, Pageable pageable);
}
