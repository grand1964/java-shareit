package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.request.ItemRequest;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class Item {
    Long id;
    String name;
    String description;
    Boolean available;
    long owner;
    ItemRequest request;
}
