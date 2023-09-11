package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Класс для просмотра вещей с запросами
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOutRequestDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
