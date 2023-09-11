package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Класс для входной информации
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestInDto {
    private String description;
}
