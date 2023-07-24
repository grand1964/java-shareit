package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ItemController {
    private static final String HEADER_NAME = "X-Sharer-User-Id";
    private final ItemService service;

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех вещей
    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(HEADER_NAME) Long ownerId) {
        log.info("Запрошено получение всех вещей владельца с идентификатором " + ownerId);
        return service.getAllItems(ownerId);
    }

    //получение вещи по идентификатору
    @GetMapping(value = "/{id}")
    public ItemDto getItem(@PathVariable("id") long itemId) {
        log.info("Запрошена вещь с идентификатором " + itemId);
        return service.getItem(itemId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @PostMapping
    public ItemDto createItem(@Valid @NotNull @RequestHeader(HEADER_NAME) long ownerId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info("Запрошено создание новой вещи владельца с идентификатором " + ownerId);
        return service.createItem(ownerId, itemDto); //здесь поля проверяются автоматически
    }

    @PatchMapping(value = "/{id}")
    public ItemDto patchItem(@PathVariable("id") long itemId,
                             @Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId,
                             @RequestBody ItemDto itemDto) {
        log.info("Запрошено обновление вещи с идентификатором " + itemId);
        itemDto.setId(itemId);
        return service.patchItem(ownerId, itemDto); //здесь поля проверяются в service
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @DeleteMapping(value = "/{id}")
    public void deleteItem(@PathVariable long id) {
        log.info("Запрошено удаление вещи с идентификатором " + id);
        service.deleteItem(id);
    }

    @DeleteMapping
    public void deleteAllItems() {
        log.info("Запрошено удаление всех вещей.");
        service.deleteAllItems();
    }

    ////////////////////////////////// Поиск /////////////////////////////////

    @GetMapping(value = "/search")
    public List<ItemDto> searchItems(@RequestParam(defaultValue = "") String text) {
        log.info("Запрошен поиск вещи по образцу.");
        return service.searchItems(text);
    }
}
