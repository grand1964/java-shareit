package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

public interface ItemWithComments {
    Item getItem();

    Comment getComment();
}
