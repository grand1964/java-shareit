package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static ru.practicum.shareit.item.dto.ItemDtoMapper.listToItemDto;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    ////////////////////////////////// CRUD //////////////////////////////////

    @Override
    public ItemBookedDto getItem(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с идентификатором " + itemId + " не найдена."));
        Timestamp now = Timestamp.from(Instant.now());
        ItemBookedDto itemDto = ItemDtoMapper.toItemBookedDto(item);
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
    public List<ItemBookedDto> getAllItems(Long ownerId) {
        if (!userRepository.existsById(ownerId)) { //владелец некорректный
            throw new NotFoundException("Недопустимый владелец с идентификатором " + ownerId);
        }
        Timestamp now = Timestamp.from(Instant.now());
        //создаем выходной массив вещей
        List<ItemBookedDto> itemsDto = new ArrayList<>();
        //создаем отображение вещей в список комментариев
        Map<Item, List<Comment>> allComments = ListConverter.pairsToMap(
                itemRepository.getAllCommentsByOwner(ownerId));
        //создаем отображения вещей в бронирования
        Map<Item, Booking> lastMap = ListConverter.pairsToItems(
                itemRepository.getAllLastBookingsByOwner(ownerId, now));
        Map<Item, Booking> nextMap = ListConverter.pairsToItems(
                itemRepository.getAllNextBookingsByOwner(ownerId, now));
        //получаем список всех вещей владельца
        List<Item> items = itemRepository.findByOwner_Id(ownerId);
        //заполняем выходной массив
        for (Item item : items) {
            ItemBookedDto itemDto = ItemDtoMapper.toItemBookedDto(item);
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
        Item item = ItemDtoMapper.toItem(owner, itemInDto);
        log.info("Создана новая вещь с идентификатором " + item.getId());
        return ItemDtoMapper.toItemOutDto(itemRepository.save(item));
    }

    @Override
    public ItemBookedDto patchItem(Long ownerId, ItemInDto itemInDto) {
        User owner = userRepository.findById(ownerId).orElseThrow(
                () -> new NotFoundException("Недопустимый владелец с идентификатором " + ownerId)
        );
        long itemId = itemInDto.getId();
        //получаем старую вещь с заданным идентификатором
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new BadRequestException("Вещь с идентификатором " + itemId + "не найдена."));
        //проверяем владельца вещи
        if (!(oldItem.getOwner()).equals(owner)) { //вещь пытается редактировать не владелец
            throw new ForbiddenException("Пользователь " + owner + " не может редактировать эту вещь.");
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
        return ItemDtoMapper.toItemBookedDto(itemRepository.save(oldItem));
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
    public List<ItemOutDto> searchItems(String text) {
        if (text.isBlank()) { //образец поиска не задан
            return new ArrayList<>(); //так требует Postman, хотя это странно
        } else {
            return listToItemDto(itemRepository.searchItems(text));
        }
    }
}
