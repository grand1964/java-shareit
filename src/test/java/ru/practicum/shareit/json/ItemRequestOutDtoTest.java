package ru.practicum.shareit.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemOutRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ItemRequestOutDtoTest {
    private LocalDateTime created;
    private ItemRequestOutDto requestOutDto;

    @Autowired
    private JacksonTester<ItemRequestOutDto> json;

    @BeforeEach
    public void setUp() {
        long requestId = 1L;
        //создаем список вещей
        List<ItemOutRequestDto> items = new ArrayList<>();
        items.add(new ItemOutRequestDto(1L, "notebook", "ASUS", true, requestId));
        items.add(new ItemOutRequestDto(2L, "smartphone", "Samsung", true, requestId));
        //задаем время создания запроса
        created = LocalDateTime.now();
        requestOutDto = new ItemRequestOutDto(
                requestId, "Request of item", LocalDateTime.now(), items);
    }

    ///////////////////////// Проверка преобразования ////////////////////////

    @Test
    void testUserDto() throws Exception {
        String createdOrigin = convertTime(created);
        JsonContent<ItemRequestOutDto> result = json.write(requestOutDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Request of item");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(createdOrigin);
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(2);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("notebook");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description").isEqualTo("ASUS");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[1].id").isEqualTo(2);
    }

    ////////////////////////////// Формат времени ////////////////////////////

    private String convertTime(LocalDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(dateTime);
    }
}
