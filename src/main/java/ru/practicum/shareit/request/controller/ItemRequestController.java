package ru.practicum.shareit.request.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class ItemRequestController {
    private static final String HEADER_NAME = "X-Sharer-User-Id";
    private final ItemRequestService service;

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех собственных запросов
    @GetMapping
    public List<ItemRequestOutDto> getRequestsByOwner(@Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId) {
        log.info("Требуются все запросы пользователя с идентификатором " + ownerId);
        return service.getAllByOwner(ownerId);
    }

    //постраничное получение запросов всех пользователей
    @GetMapping(value = "/all")
    public List<ItemRequestOutDto> getAllRequests(@RequestHeader(HEADER_NAME) Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("постраничное получение запросов всех пользователей");
        return service.getAll(userId, from, size);
    }

    //получение запроса по его идентификатору
    @GetMapping(value = "/{requestId}")
    public ItemRequestOutDto getRequest(@PathVariable("requestId") long requestId,
                                        @Valid @NotNull @RequestHeader(HEADER_NAME) Long userId) {
        log.info("Требуется запрос с идентификатором " + requestId);
        return service.get(requestId, userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание новой брони
    @PostMapping
    public ItemRequestOutCreationDto createItem(@Valid @NotNull @RequestHeader(HEADER_NAME) long requesterId,
                                                @Valid @RequestBody ItemRequestInDto requestInDto) {
        log.info("Создание нового запроса пользователем с идентификатором " + requesterId);
        return service.createRequest(requesterId, requestInDto);
    }
}
