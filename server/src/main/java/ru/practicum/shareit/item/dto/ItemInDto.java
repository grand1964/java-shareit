package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Класс для входящей информации
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInDto {
    Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
