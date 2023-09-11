package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String HEADER_NAME = "X-Sharer-User-Id";

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех вещей
    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader(HEADER_NAME) Long ownerId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "20") @Positive int size) {
        validateId(ownerId);
        log.info("Запрошено получение всех вещей владельца с идентификатором {}", ownerId);
        return itemClient.getAllItems(ownerId, from, size);
    }

    //получение вещи по идентификатору
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getItem(@RequestHeader(HEADER_NAME) long userId,
                                          @PathVariable("id") long itemId) {
        validateId(itemId);
        log.info("Запрошена вещь с идентификатором {} пользователем {}", itemId, userId);
        return itemClient.getItem(itemId, userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание новой вещи
    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId,
                                             @Valid @RequestBody ItemInDto itemInDto) {
        validateId(ownerId);
        log.info("Запрошено создание новой вещи владельца с идентификатором " + ownerId);
        return itemClient.createItem(ownerId, itemInDto);
    }

    //обновление вещи
    @PatchMapping(value = "/{id}")
    public ResponseEntity<Object> patchItem(@Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId,
                                            @PathVariable("id") @Positive Long itemId,
                                            @RequestBody ItemInDto itemInDto) { //не проверяем корректность!
        validateId(ownerId);
        log.info("Запрошено обновление вещи с идентификатором {}", itemId);
        itemInDto.setId(itemId);
        return itemClient.patchItem(itemId, ownerId, itemInDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@Valid @NotNull @RequestHeader(HEADER_NAME) Long authorId,
                                                @PathVariable("itemId") Long itemId,
                                                @Valid @RequestBody CommentInDto commentInDto) {
        validateId(authorId, itemId);
        log.info("Запрошено создание комментария к вещи {} пользователем {}", itemId, authorId);
        return itemClient.createComment(itemId, authorId, commentInDto);
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable long id) {
        log.info("Запрошено удаление вещи с идентификатором {}", id);
        return itemClient.deleteItem(id);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteAllItems() {
        log.info("Запрошено удаление всех вещей.");
        return itemClient.deleteAllItems();
    }

    ////////////////////////////////// Поиск /////////////////////////////////

    @GetMapping(value = "/search")
    public ResponseEntity<Object> searchItems(@RequestParam(defaultValue = "") String text,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("Запрошен поиск вещи по образцу.");
        return itemClient.searchItems(text, from, size);
    }

    ///////////////////////// Валидация идентификаторов ///////////////////////

    private void validateId(long... ids) {
        for (long id : ids) {
            if (id <= 0) {
                throw new NotFoundException("Идентификатор должен быть положительным");
            }
        }
    }
}
