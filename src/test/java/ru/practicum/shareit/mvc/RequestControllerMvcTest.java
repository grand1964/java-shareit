package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemOutRequestDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class RequestControllerMvcTest {
    private static HttpHeaders headers;
    private static final long requestId = 1L;
    private static ItemRequestInDto requestInDto;
    private static ItemRequestOutDto requestOutDto;
    private static ItemRequestOutCreationDto requestOutCreationDto;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService requestService;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        //создаем дополнительный заголовок
        headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", "1");
        //создаем список вещей
        List<ItemOutRequestDto> items = new ArrayList<>();
        items.add(new ItemOutRequestDto(1L, "notebook", "ASUS", true, requestId));
        items.add(new ItemOutRequestDto(2L, "smartphone", "Samsung", true, requestId));
        //создаем входной запрос
        requestInDto = new ItemRequestInDto("Request of item");
        //создаем выходные запросы
        requestOutCreationDto = new ItemRequestOutCreationDto(
                requestId, "Request of item", LocalDateTime.now());
        requestOutDto = new ItemRequestOutDto(
                requestId, "Request of item", LocalDateTime.now(), items);
    }

    ///////////////////////////// Получение данных ///////////////////////////

    @Test
    void getRequestByIdTest() throws Exception {
        when(requestService.get(anyLong(), anyLong()))
                .thenReturn(requestOutDto);

        mvc.perform(get("/requests/{id}", requestId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestOutDto.getDescription())))
                .andExpect(jsonPath("$.created", is(convertTime(requestOutDto.getCreated()))))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$.items[0].name", is("notebook")))
                .andExpect(jsonPath("$.items[0].description", is("ASUS")))
                .andExpect(jsonPath("$.items[0].available", is(true), Boolean.class))
                .andExpect(jsonPath("$.items[0].requestId", is(requestId), Long.class))
                .andExpect(jsonPath("$.items[1].id", is(2L), Long.class))
                .andExpect(jsonPath("$.items[1].name", is("smartphone")))
                .andExpect(jsonPath("$.items[1].description", is("Samsung")))
                .andExpect(jsonPath("$.items[1].available", is(true), Boolean.class))
                .andExpect(jsonPath("$.items[1].requestId", is(requestId), Long.class));
    }

    @Test
    void geAllRequestsByOwnerTest() throws Exception {
        when(requestService.getAllByOwner(anyLong()))
                .thenReturn(List.of(requestOutDto));

        mvc.perform(get("/requests")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestOutDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(convertTime(requestOutDto.getCreated()))))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is("notebook")))
                .andExpect(jsonPath("$[0].items[0].description", is("ASUS")))
                .andExpect(jsonPath("$[0].items[0].available", is(true), Boolean.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(requestId), Long.class))
                .andExpect(jsonPath("$[0].items[1].id", is(2L), Long.class))
                .andExpect(jsonPath("$[0].items[1].name", is("smartphone")))
                .andExpect(jsonPath("$[0].items[1].description", is("Samsung")))
                .andExpect(jsonPath("$[0].items[1].available", is(true), Boolean.class))
                .andExpect(jsonPath("$[0].items[1].requestId", is(requestId), Long.class));
    }

    @Test
    void geAllRequestsTest() throws Exception {
        when(requestService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestOutDto));

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestOutDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(convertTime(requestOutDto.getCreated()))))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is("notebook")))
                .andExpect(jsonPath("$[0].items[0].description", is("ASUS")))
                .andExpect(jsonPath("$[0].items[0].available", is(true), Boolean.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(requestId), Long.class))
                .andExpect(jsonPath("$[0].items[1].id", is(2L), Long.class))
                .andExpect(jsonPath("$[0].items[1].name", is("smartphone")))
                .andExpect(jsonPath("$[0].items[1].description", is("Samsung")))
                .andExpect(jsonPath("$[0].items[1].available", is(true), Boolean.class))
                .andExpect(jsonPath("$[0].items[1].requestId", is(requestId), Long.class));
    }

    ///////////////////////////// Создание запроса ///////////////////////////

    @Test
    void createRequestTest() throws Exception {
        when(requestService.createRequest(anyLong(), any(ItemRequestInDto.class)))
                .thenReturn(requestOutCreationDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestOutCreationDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestOutCreationDto.getDescription())))
                .andExpect(jsonPath("$.created", is(convertTime(requestOutCreationDto.getCreated()))));
    }

    ///////////////////// Тесты корректности параметров //////////////////////

    @Test
    void createRequestWithEmptyDescriptionTest() throws Exception {
        ItemRequestInDto badInDto = new ItemRequestInDto(" ");
        when(requestService.createRequest(anyLong(), any(ItemRequestInDto.class)))
                .thenReturn(requestOutCreationDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(badInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllRequestsWithBadFromParameterTest() throws Exception {
        when(requestService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestOutDto));

        mvc.perform(get("/requests/all?from={from}&size={size}", -1, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllRequestsWithBadSizeParameterTest() throws Exception {
        when(requestService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestOutDto));

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, -20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    ////////////////////////////// Формат времени ////////////////////////////

    private String convertTime(LocalDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(dateTime);
    }
}
