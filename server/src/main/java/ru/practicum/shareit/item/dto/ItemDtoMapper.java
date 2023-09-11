package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemDtoMapper {
    public static ItemOutDto toItemOutDto(Item item) {
        ItemOutDto dto = new ItemOutDto(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), null, new ArrayList<>());
        if (item.getRequest() != null) { //вещь с запросом
            dto.setRequestId(item.getRequest().getId()); //устанавливаем идентификатор запроса
        }
        return dto; //возвращаем объект
    }

    public static ItemOutRequestDto toItemOutRequestDto(Item item) {
        //в этом контексте вещь не может быть без запроса
        return new ItemOutRequestDto(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), item.getRequest().getId());
    }

    public static ItemOutBookedDto toItemOutBookedDto(Item item) {
        ItemOutBookedDto dto = new ItemOutBookedDto(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), null, null, null, new ArrayList<>());
        if (item.getRequest() != null) { //вещь с запросом
            dto.setRequestId(item.getRequest().getId()); //устанавливаем идентификатор запроса
        }
        return dto; //возвращаем объект
    }

    public static Item toItem(User owner, ItemRequest request, ItemInDto itemInDto) {
        Item item = new Item();
        item.setId(itemInDto.getId());
        item.setName(itemInDto.getName());
        item.setDescription(itemInDto.getDescription());
        item.setAvailable(itemInDto.getAvailable());
        item.setOwner(owner);
        item.setRequest(request); //request может быть и null
        return item;
    }

    public static List<ItemOutRequestDto> listToItemRequestDto(List<Item> list) {
        return list.stream()
                .filter(Objects::nonNull) //из базы могут приходить запросы без ответов
                .map(ItemDtoMapper::toItemOutRequestDto)
                .collect(Collectors.toList());
    }
}
