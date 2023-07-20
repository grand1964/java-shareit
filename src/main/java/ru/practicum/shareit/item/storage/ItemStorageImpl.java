package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemStorageImpl implements ItemStorage {
    private long currentId;
    private final Map<Long, Item> items;

    public ItemStorageImpl() {
        currentId = 0;
        items = new HashMap<>();
    }

    @Override
    public boolean containsId(long id) {
        return items.containsKey(id);
    }

    @Override
    public Optional<Item> get(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> getAll(long ownerId) {
        return items.values().stream()
                .filter(i -> i.getOwner() == ownerId)
                .sorted(Comparator.comparingLong(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public void create(Item item) {
        long id = getId();
        item.setId(id);
        items.put(id, item);
    }

    @Override
    public void update(Item item) {
        items.put(item.getId(), item);
    }

    @Override
    public boolean delete(long id) {
        if (containsId(id)) {
            items.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int deleteAll() {
        int size = items.size();
        items.clear();
        currentId = 0;
        return size;
    }

    @Override
    public List<Item> searchItems(String text) {
        return items.values().stream()
                .filter(i -> i.getAvailable() &&
                        ((i.getName().toLowerCase().contains(text.toLowerCase())) ||
                                (i.getDescription().toLowerCase().contains(text.toLowerCase()))))
                .sorted(Comparator.comparingLong(Item::getId))
                .collect(Collectors.toList());
    }

    private long getId() {
        return ++currentId;
    }
}
