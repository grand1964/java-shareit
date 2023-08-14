package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemWithBooking;
import ru.practicum.shareit.item.storage.ItemWithComments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListConverter {
    public static Map<Item, Booking> itemsToBookings(List<ItemWithBooking> list) {
        Map<Item, Booking> map = new HashMap<>();
        for (ItemWithBooking entry : list) {
            map.put(entry.getItem(), entry.getBooking());
        }
        return map;
    }

    public static Map<Item, List<Comment>> itemToComments(List<ItemWithComments> list) {
        Map<Item, List<Comment>> map = new HashMap<>();
        for (ItemWithComments entry : list) {
            Item item = entry.getItem();
            Comment comment = entry.getComment();
            if (map.containsKey(item)) {
                map.get(item).add(comment);
            } else {
                List<Comment> comments = new ArrayList<>();
                comments.add(comment);
                map.put(item, comments);
            }
        }
        return map;
    }
}
