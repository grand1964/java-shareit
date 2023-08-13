package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

public interface ItemWithBooking {
    Item getItem();
    Booking getBooking();
}
