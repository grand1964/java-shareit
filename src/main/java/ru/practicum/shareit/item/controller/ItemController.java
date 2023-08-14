package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.CommentService;
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
    private final ItemService itemService;
    private final CommentService commentService;

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех вещей
    @GetMapping
    public List<ItemBookedDto> getAllItems(@RequestHeader(HEADER_NAME) Long ownerId) {
        log.info("Запрошено получение всех вещей владельца с идентификатором " + ownerId);
        return itemService.getAllItems(ownerId);
    }

    //получение вещи по идентификатору
    @GetMapping(value = "/{id}")
    public ItemBookedDto getItem(@PathVariable("id") long itemId, @RequestHeader(HEADER_NAME) long userId) {
        log.info("Запрошена вещь с идентификатором " + itemId + " пользователем " + userId);
        return itemService.getItem(itemId, userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @PostMapping
    public ItemOutDto createItem(@Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId,
                                 @Valid @RequestBody ItemInDto itemInDto) {
        log.info("Запрошено создание новой вещи владельца с идентификатором " + ownerId);
        return itemService.createItem(ownerId, itemInDto); //здесь поля проверяются автоматически
    }

    @PatchMapping(value = "/{id}")
    public ItemBookedDto patchItem(@PathVariable("id") Long itemId,
                                   @Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId,
                                   @RequestBody ItemInDto itemInDto) {
        log.info("Запрошено обновление вещи с идентификатором " + itemId);
        itemInDto.setId(itemId);
        return itemService.patchItem(ownerId, itemInDto); //здесь поля проверяются в itemService
    }

    @PostMapping("/{itemId}/comment")
    public CommentOutDto createComment(@PathVariable("itemId") Long itemId,
                                       @Valid @NotNull @RequestHeader(HEADER_NAME) Long authorId,
                                       @Valid @RequestBody CommentInDto commentInDto) {
        log.info("Запрошено создание комментария к вещи " + itemId + " пользователем " + authorId);
        return commentService.createComment(itemId, authorId, commentInDto);
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @DeleteMapping(value = "/{id}")
    public void deleteItem(@PathVariable long id) {
        log.info("Запрошено удаление вещи с идентификатором " + id);
        itemService.deleteItem(id);
    }

    @DeleteMapping
    public void deleteAllItems() {
        log.info("Запрошено удаление всех вещей.");
        itemService.deleteAllItems();
    }

    ////////////////////////////////// Поиск /////////////////////////////////

    @GetMapping(value = "/search")
    public List<ItemOutDto> searchItems(@RequestParam(defaultValue = "") String text) {
        log.info("Запрошен поиск вещи по образцу.");
        return itemService.searchItems(text);
    }
}
