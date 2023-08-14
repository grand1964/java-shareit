package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.CommentOutDto;

public interface CommentService {
    CommentOutDto createComment(Long itemId, Long authorId, CommentInDto itemDto);
}
