package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    @Autowired
    public ItemController(ItemService service) {
        this.service = service;
    }

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех вещей
    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return service.getAllItems(ownerId);
    }

    //получение вещи по идентификатору
    @GetMapping(value = "/{id}")
    public ItemDto getItem(@PathVariable("id") long itemId) {
        return service.getItem(itemId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") long ownerId,
                              @Valid @RequestBody ItemDto itemDto) {
        return service.createItem(ownerId, itemDto); //здесь поля проверяются автоматически
    }

    @PatchMapping(value = "/{id}")
    public ItemDto patchItem(@PathVariable("id") long itemId,
                             @RequestHeader("X-Sharer-User-Id") Long ownerId, @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        return service.patchItem(ownerId, itemDto); //здесь поля проверяются в service
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @DeleteMapping(value = "/{id}")
    public void deleteItem(@PathVariable long id) {
        service.deleteItem(id);
    }

    @DeleteMapping
    public void deleteAllItems() {
        service.deleteAllItems();
    }

    ////////////////////////////////// Поиск /////////////////////////////////

    @GetMapping(value = "/search")
    public List<ItemDto> searchItems(@RequestParam(defaultValue = "") String text) {
        return service.searchItems(text);
    }
}
