package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.convert.ListConverter;
import ru.practicum.shareit.common.convert.PairToReturn;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;

    //компаратор для сортировки запросов по времени
    private final Comparator<ItemRequestOutDto> requestComparator =
            (a, b) -> {
                if (a.getCreated().isBefore(b.getCreated())) {
                    return 1;
                } else if (a.getCreated().isAfter(b.getCreated())) {
                    return -1;
                } else {
                    return 0;
                }
            };

    @Override
    public ItemRequestOutDto get(long requestId, long userId) {
        //проверяем пользователя
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Недопустимый пользователь с идентификатором " + userId);
        }
        //получаем и проверяем запрос
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Недопустимый запрос с идентификатором " + requestId)
        );
        //преобразуем его в dto
        ItemRequestOutDto dto = ItemRequestDtoMapper.toItemRequestDto(request);
        //получаем список вещей для данного запроса
        List<Item> items = itemRepository.findByRequest_Id(requestId);
        //устанавливаем его в dto и возвращаем результат
        dto.setItems(ItemDtoMapper.listToItemRequestDto(items));
        return dto;
    }

    @Override
    public List<ItemRequestOutDto> getAll(long userId, int from, int size) {
        //проверяем пользователя
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Недопустимый пользователь с идентификатором " + userId);
        }
        //выполняем запрос с пагинацией, но без результатов
        PageRequest pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> requestsPage = requestRepository.findAll(pageable).getContent();
        //делаем выборку пар (запрос, вещь) для запросов из полученной страницы
        List<PairToReturn<ItemRequest, Item>> pairs = requestRepository.getAllRequestPairs(userId, requestsPage);
        //обрабатываем пары и возвращаем результат
        return processPairs(pairs);
    }

    @Override
    public List<ItemRequestOutDto> getAllByOwner(Long ownerId) {
        //проверяем владельца
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException("Недопустимый владелец с идентификатором " + ownerId);
        }
        //делаем выборку пар (запрос, вещь) для заданного хозяина запросов
        List<PairToReturn<ItemRequest, Item>> pairs = requestRepository.getAllRequestPairsByOwner(ownerId);
        //обрабатываем пары и возвращаем результат
        return processPairs(pairs);
    }

    @Override
    public ItemRequestOutCreationDto createRequest(Long authorId, ItemRequestInDto requestInDto) {
        //получаем и проверяем автора запроса
        User author = userRepository.findById(authorId).orElseThrow(
                () -> new NotFoundException("Недопустимый автор запроса с идентификатором " + authorId)
        );
        ItemRequest request = requestRepository.save(ItemRequestDtoMapper.toItemRequest(author, requestInDto));
        log.info("Создан новый запрос с идентификатором " + request.getId());
        return ItemRequestDtoMapper.toItemRequestCreationDto(request);
    }

    public void updateCreated(long requestId, long additionalMillis) {
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(
                () -> new BadRequestException("Запрос с идентификатором " + requestId + " не найден.")
        );
        request.setCreated(Timestamp.from(request.getCreated().toInstant().plusMillis(additionalMillis)));
    }

    ////////////////////////// Обработка списка пар //////////////////////////

    private List<ItemRequestOutDto> processPairs(List<PairToReturn<ItemRequest, Item>> pairs) {
        //преобразуем список пар в запросы с результатами
        Map<ItemRequest, List<Item>> requests = ListConverter.keyToValues(pairs);
        //создаем и заполняем выходной список
        List<ItemRequestOutDto> listDto = new ArrayList<>();
        for (ItemRequest request : requests.keySet()) {
            //извлекаем очередной запрос и преобразуем его в dto
            ItemRequestOutDto requestOutDto = ItemRequestDtoMapper.toItemRequestDto(request);
            //преобразуем результаты запроса в dto и сохраняем их в выходном запросе
            requestOutDto.setItems(ItemDtoMapper.listToItemRequestDto(requests.get(request)));
            //запоминаем выходной запрос
            listDto.add(requestOutDto);
        }
        //сортируем по убыванию даты создания и возвращаем
        return listDto.stream().sorted(requestComparator).collect(Collectors.toList());
    }
}
