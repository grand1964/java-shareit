package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    boolean containsId(long id);

    Optional<Item> get(long id);

    List<Item> getAll(long ownerId);

    void create(Item item);

    void update(Item item);

    boolean delete(long id);

    int deleteAll();

    List<Item> searchItems(String sample);
}
