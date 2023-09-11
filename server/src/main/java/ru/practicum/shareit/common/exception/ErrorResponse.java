package ru.practicum.shareit.common.exception;

import lombok.Value;

@Value
public class ErrorResponse {
    // название ошибки
    String error;
    // подробное описание
    String description;
}
