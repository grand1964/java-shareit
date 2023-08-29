package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemOutRequestDto;

import javax.validation.constraints.Past;
import java.time.LocalDateTime;
import java.util.List;

//Класс для возврата запроса с результатами
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestOutDto {
    private Long id;
    private String description;
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
    private List<ItemOutRequestDto> items;
}
