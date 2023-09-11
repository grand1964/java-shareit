package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Past;
import java.time.LocalDateTime;

//Класс для ответа на создание запроса
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestOutDto {
    private Long id;
    private String description;
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
}
