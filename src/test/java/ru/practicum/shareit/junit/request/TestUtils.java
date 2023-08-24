package ru.practicum.shareit.junit.request;

import ru.practicum.shareit.request.dto.ItemRequestInDto;

public class TestUtils {
    public static ItemRequestInDto createRequestDto(String description) {
        return new ItemRequestInDto(description);
    }
}
