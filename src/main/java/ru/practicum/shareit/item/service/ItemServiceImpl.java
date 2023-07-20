package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
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
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    ////////////////////////////////// CRUD //////////////////////////////////

    @Override
    public ItemDto getItem(long id) { //владелец здесь неважен
        return ItemDtoMapper.toItemDto(itemStorage.get(id)
                .orElseThrow(() -> badItem("Вещь с идентификатором " + id + "не найдена.")));
    }

    @Override
    public List<ItemDto> getAllItems(Long ownerId) {
        if (!userStorage.containsId(ownerId)) { //владелец некорректный
            badOwner(ownerId);
        }
        log.info("Получен список всех вещей пользователя " + ownerId);
        return ItemDtoMapper.listToItemDto(itemStorage.getAll(ownerId));
    }

    @Override
    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (ownerId == null) { //владелец не задан
            emptyOwner();
        }
        if (!userStorage.containsId(ownerId)) { //владелец некорректный
            badOwner(ownerId);
        }
        Item item = ItemDtoMapper.toItem(ownerId, itemDto);
        itemStorage.create(item);
        log.info("Создана новая вещь с идентификатором " + item.getId());
        return ItemDtoMapper.toItemDto(item);
    }

    @Override
    public ItemDto patchItem(Long ownerId, ItemDto itemDto) {
        if (ownerId == null) { //владелец не задан
            emptyOwner();
        }
        if (!userStorage.containsId(ownerId)) { //владелец некорректный
            badOwner(ownerId);
        }
        long itemId = itemDto.getId();
        //получаем старую вещь с заданным идентификатором
        Item oldItem = itemStorage.get(itemId)
                .orElseThrow(() -> badItem("Вещь с идентификатором " + itemId + "не найдена."));
        //проверяем владельца вещи
        if (!((Long) oldItem.getOwner()).equals(ownerId)) { //вещь пытается редактировать не владелец
            forbiddenOwner(ownerId); //ошибка
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
        if ("".equals(text)) { //образец поиска не задан
            return new ArrayList<>(); //так требует Postman, хотя это странно
        } else {
            return ItemDtoMapper.listToItemDto(itemStorage.searchItems(text));
        }
    }

    //////////////////////////////// Валидация ///////////////////////////////

    //диагностика ошибочной вещи
    private RuntimeException badItem(String message) {
        log.error(message);
        return new BadRequestException(message);
    }

    //диагностика ошибочного владельца
    private void badOwner(Long id) {
        String message = "Недопустимый владелец с идентификатором " + id;
        log.error(message);
        throw new NotFoundException(message);
    }

    //диагностика редактирования не владельцем
    private void forbiddenOwner(Long id) {
        String message = "Пользователь " + id + " не может редактировать эту вещь.";
        log.error(message);
        throw new ForbiddenException(message);
    }

    //диагностика отсутствующего владельца
    private void emptyOwner() {
        String message = "Не указан владелец вещи.";
        log.error(message);
        throw new BadRequestException(message);
    }
}
