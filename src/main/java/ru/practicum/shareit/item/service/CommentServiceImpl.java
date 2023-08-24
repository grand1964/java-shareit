package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDtoMapper;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CommentServiceImpl implements CommentService {
    private UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private CommentRepository commentRepository;

    @Override
    public CommentOutDto createComment(Long itemId, Long authorId, CommentInDto commentDto) {
        User author = userRepository.findById(authorId).orElseThrow(
                () -> new NotFoundException("Недопустимый автор комментария с идентификатором " + authorId)
        );
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Недопустимая вещь с идентификатором " + itemId)
        );
        List<Booking> bookings = bookingRepository.findByBooker_IdAndEndBeforeAndStatusIs(
                authorId, Timestamp.from(Instant.now()), Status.APPROVED);
        if (bookings.isEmpty()) {
            throw new BadRequestException("Автор " + authorId + "не арендовал вещь или срок аренды не истек.");
        }
        //если все проверки прошли - добавляем комментарий
        Comment comment = CommentDtoMapper.toComment(item, author, commentDto);
        commentRepository.save(comment);
        log.info("Создан новый комментарий к вещи с идентификатором " + item.getId());
        return CommentDtoMapper.toCommentOutDto(comment);
    }
}
