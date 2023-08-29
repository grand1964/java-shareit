package ru.practicum.shareit.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    // название ошибки
    private String error;
    // подробное описание
    private String description;
}
