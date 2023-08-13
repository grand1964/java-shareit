package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDtoMapper {
    public static CommentOutDto toCommentOutDto(Comment comment) {
        return new CommentOutDto(comment.getId(), comment.getText(),
                comment.getAuthor().getName(), comment.getCreated().toLocalDateTime());
    }

    public static Comment toComment(Item item, User author, CommentInDto commentDto) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(Timestamp.from(Instant.now()));
        return comment;
    }

    public static List<CommentOutDto> listToCommentDto(List<Comment> list) {
        return list.stream().map(CommentDtoMapper::toCommentOutDto).collect(Collectors.toList());
    }
}
