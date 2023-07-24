package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    ////////////////////////////////// CRUD //////////////////////////////////

    @Override
    public ItemDto getItem(long id) { //владелец здесь неважен
        return ItemDtoMapper.toItemDto(itemStorage.get(id)
                .orElseThrow(() -> new BadRequestException("Вещь с идентификатором " + id + "не найдена.")));
    }

    @Override
    public List<ItemDto> getAllItems(Long ownerId) {
        if (!userStorage.containsId(ownerId)) { //владелец некорректный
            throw new NotFoundException("Недопустимый владелец с идентификатором " + ownerId);
        }
        log.info("Получен список всех вещей пользователя " + ownerId);
        return ItemDtoMapper.listToItemDto(itemStorage.getAll(ownerId));
    }

    @Override
    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (!userStorage.containsId(ownerId)) { //владелец некорректный
            throw new NotFoundException("Недопустимый владелец с идентификатором " + ownerId);
        }
        Item item = ItemDtoMapper.toItem(ownerId, itemDto);
        itemStorage.create(item);
        log.info("Создана новая вещь с идентификатором " + item.getId());
        return ItemDtoMapper.toItemDto(item);
    }

    @Override
    public ItemDto patchItem(Long ownerId, ItemDto itemDto) {
        if (!userStorage.containsId(ownerId)) { //владелец некорректный
            throw new NotFoundException("Недопустимый владелец с идентификатором " + ownerId);
        }
        long itemId = itemDto.getId();
        //получаем старую вещь с заданным идентификатором
        Item oldItem = itemStorage.get(itemId)
                .orElseThrow(() -> new BadRequestException("Вещь с идентификатором " + itemId + "не найдена."));
        //проверяем владельца вещи
        if (!((Long) oldItem.getOwner()).equals(ownerId)) { //вещь пытается редактировать не владелец
            throw new ForbiddenException("Пользователь " + ownerId + " не может редактировать эту вещь.");
        }
        //если все корректно - переустанавливаем поля
        String name = itemDto.getName();
        if ((name != null) && !name.isBlank()) {
            oldItem.setName(name);
        }
        String description = itemDto.getDescription();
        if ((description != null) && !description.isBlank()) {
            oldItem.setDescription(description);
        }
        Boolean available = itemDto.getAvailable();
        if (available != null) {
            oldItem.setAvailable(available);
        }
        itemStorage.update(oldItem);
        log.info("Обновлена вещь с идентификатором " + oldItem.getId());
        return ItemDtoMapper.toItemDto(oldItem);
    }

    @Override
    public void deleteItem(long id) {
        if (itemStorage.delete(id)) {
            log.info("Удалена вещь с идентификатором " + id);
        } else {
            log.warn("Вещь с идентификатором " + id + "не найдена.");
        }
    }

    @Override
    public void deleteAllItems() {
        int count = itemStorage.deleteAll();
        log.info("Удалено " + count + " вещей.");
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) { //образец поиска не задан
            return new ArrayList<>(); //так требует Postman, хотя это странно
        } else {
            return ItemDtoMapper.listToItemDto(itemStorage.searchItems(text));
        }
    }
}
