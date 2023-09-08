package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.convert.ListConverter;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    ////////////////////////////////// CRUD //////////////////////////////////

    @Override
    public ItemOutBookedDto getItem(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с идентификатором " + itemId + " не найдена."));
        Timestamp now = Timestamp.from(Instant.now());
        ItemOutBookedDto itemDto = ItemDtoMapper.toItemOutBookedDto(item);
        //бронирования априори пусты
        itemDto.setLastBooking(null);
        itemDto.setNextBooking(null);
        if (userId == item.getOwner().getId()) { //ищем их только для владельца
            List<Booking> lastBookings = itemRepository.getLastBookingOfItem(itemId, now);
            if (!lastBookings.isEmpty()) {
                itemDto.setLastBooking(BookingDtoMapper.toBookingDto(lastBookings.get(0)));
            }
            List<Booking> nextBookings = itemRepository.getNextBookingOfItem(itemId, now);
            if (!nextBookings.isEmpty()) {
                itemDto.setNextBooking(BookingDtoMapper.toBookingDto(nextBookings.get(0)));
            }
        }
        itemDto.setComments(CommentDtoMapper.listToCommentDto(commentRepository.findByItem_Id(itemId)));
        return itemDto;
    }

    @Override
    public List<ItemOutBookedDto> getAllItems(Long ownerId, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) { //владелец некорректный
            throw new NotFoundException("Недопустимый владелец с идентификатором " + ownerId);
        }
        Timestamp now = Timestamp.from(Instant.now());
        //создаем выходной массив вещей
        List<ItemOutBookedDto> itemsDto = new ArrayList<>();
        //создаем отображение вещей в список комментариев
        Map<Item, List<Comment>> allComments = ListConverter.keyToValues(
                itemRepository.getAllCommentsByOwner(ownerId));
        //создаем отображения вещей в бронирования
        Map<Item, Booking> lastMap = ListConverter.keyToValue(
                itemRepository.getAllLastBookingsByOwner(ownerId, now));
        Map<Item, Booking> nextMap = ListConverter.keyToValue(
                itemRepository.getAllNextBookingsByOwner(ownerId, now));
        //получаем порцию списка всех вещей владельца
        List<Item> items = new ArrayList<>(itemRepository.findByOwner_Id(ownerId, pageable).getContent());
        items.sort(Comparator.comparingInt(i -> i.getId().intValue()));
        //заполняем выходной массив
        for (Item item : items) {
            ItemOutBookedDto itemDto = ItemDtoMapper.toItemOutBookedDto(item);
            //в отображениях вещи сравниваются с ключами по идентификаторам
            if (lastMap.containsKey(item)) {
                itemDto.setLastBooking(BookingDtoMapper.toBookingDto(lastMap.get(item)));
            }
            if (nextMap.containsKey(item)) {
                itemDto.setNextBooking(BookingDtoMapper.toBookingDto(nextMap.get(item)));
            }
            if (allComments.containsKey(item)) {
                itemDto.setComments(CommentDtoMapper.listToCommentDto(allComments.get(item)));
            } else {
                itemDto.setComments(new ArrayList<>());
            }
            itemsDto.add(itemDto);
        }
        log.info("Получен список всех вещей пользователя " + ownerId);
        return itemsDto;
    }

    @Override
    public ItemOutDto createItem(Long ownerId, ItemInDto itemInDto) {
        User owner = userRepository.findById(ownerId).orElseThrow(
                () -> new NotFoundException("Недопустимый владелец с идентификатором " + ownerId)
        );
        //пытаемся получить request по его идентификатору из itemInDto
        Long requestId = itemInDto.getRequestId();
        ItemRequest request;
        if (requestId != null) { //запрос есть, получаем его из базы
            request = requestRepository.findById(requestId).orElseThrow(
                    () -> new NotFoundException("Недопустимый запрос с идентификатором " + requestId)
            );
        } else { //запроса нет
            request = null;
        }
        //создаем новую вещь (запрос может быть пустым)
        Item item = itemRepository.save(ItemDtoMapper.toItem(owner, request, itemInDto));
        log.info("Создана новая вещь с идентификатором " + item.getId());
        return ItemDtoMapper.toItemOutDto(item);
    }

    @Override
    public ItemOutDto patchItem(Long ownerId, ItemInDto itemInDto) {
        User owner = userRepository.findById(ownerId).orElseThrow(
                () -> new NotFoundException("Недопустимый владелец с идентификатором " + ownerId)
        );
        long itemId = itemInDto.getId();
        //получаем старую вещь с заданным идентификатором
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new BadRequestException("Вещь с идентификатором " + itemId + "не найдена."));
        //проверяем владельца вещи
        if (!oldItem.getOwner().equals(owner)) { //вещь пытается редактировать не владелец
            throw new ForbiddenException("Пользователь " + ownerId + " не может редактировать эту вещь.");
        }
        //если все корректно - переустанавливаем поля
        String name = itemInDto.getName();
        if ((name != null) && !name.isBlank()) {
            oldItem.setName(name);
        }
        String description = itemInDto.getDescription();
        if ((description != null) && !description.isBlank()) {
            oldItem.setDescription(description);
        }
        Boolean available = itemInDto.getAvailable();
        if (available != null) {
            oldItem.setAvailable(available);
        }
        log.info("Обновлена вещь с идентификатором " + oldItem.getId());
        return ItemDtoMapper.toItemOutDto(itemRepository.save(oldItem));
    }

    @Override
    public void deleteItem(long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            log.info("Удалена вещь с идентификатором " + id);
        } else {
            log.warn("Вещь с идентификатором " + id + "не найдена.");
        }
    }

    @Override
    public void deleteAllItems() {
        long count = itemRepository.count();
        itemRepository.deleteAll();
        log.info("Удалено " + count + " вещей.");
    }

    @Override
    public List<ItemOutDto> searchItems(String text, Pageable pageable) {
        if (text.isBlank()) { //образец поиска не задан
            return new ArrayList<>(); //так требует Postman, хотя это странно
        } else { //получаем порцию данных
            return itemRepository.searchItems(text, pageable)
                    .map(ItemDtoMapper::toItemOutDto)
                    .getContent();
        }
    }
}
