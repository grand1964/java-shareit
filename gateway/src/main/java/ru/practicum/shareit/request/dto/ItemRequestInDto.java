package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

//Класс для входной информации
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestInDto {
    @NotBlank
    private String description;
}
