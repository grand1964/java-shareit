package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;

import java.util.List;

public interface ItemRequestService {

    //получение конкретного запроса с результатами
    ItemRequestOutDto get(long requestId, long userId);

    //постраничное получение всех чужих запросов результатами
    List<ItemRequestOutDto> getAll(long userId, int from, int size);

    //получение всех запросов заданного пользователя с результатами
    List<ItemRequestOutDto> getAllByOwner(Long ownerId);

    //создание нового запроса
    ItemRequestOutCreationDto createRequest(Long authorId, ItemRequestInDto requestInDto);

    //нужно для тестов
    void updateCreated(long requestId, long additionalMillis);
}
