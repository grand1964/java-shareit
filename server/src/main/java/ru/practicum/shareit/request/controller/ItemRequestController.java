package ru.practicum.shareit.request.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestController {
    private static final String HEADER_NAME = "X-Sharer-User-Id";
    private final ItemRequestService service;

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех собственных запросов
    @GetMapping
    public List<ItemRequestOutDto> getRequestsByOwner(@RequestHeader(HEADER_NAME) Long ownerId) {
        log.info("Требуются все запросы пользователя с идентификатором " + ownerId);
        return service.getAllByOwner(ownerId);
    }

    //постраничное получение запросов всех пользователей
    @GetMapping(value = "/all")
    public List<ItemRequestOutDto> getAllRequests(@RequestHeader(HEADER_NAME) Long userId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "20") int size) {
        log.info("постраничное получение запросов всех пользователей");
        return service.getAll(userId, from, size);
    }

    //получение запроса по его идентификатору
    @GetMapping(value = "/{requestId}")
    public ItemRequestOutDto getRequest(@PathVariable("requestId") long requestId,
                                        @RequestHeader(HEADER_NAME) Long userId) {
        log.info("Требуется запрос с идентификатором " + requestId);
        return service.get(requestId, userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание нового запроса
    @PostMapping
    public ItemRequestOutCreationDto createRequest(@RequestHeader(HEADER_NAME) long requesterId,
                                                   @RequestBody ItemRequestInDto requestInDto) {
        log.info("Создание нового запроса пользователем с идентификатором " + requesterId);
        return service.createRequest(requesterId, requestInDto);
    }
}
