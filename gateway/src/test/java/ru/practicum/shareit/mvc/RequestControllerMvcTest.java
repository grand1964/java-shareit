package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final String HEADER_NAME = "X-Sharer-User-Id";
    private static final long requestId = 1L;
    private static ItemRequestInDto requestInDto;
    private static ItemRequestOutDto requestOutDto;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestClient requestClient;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        //создаем входной запрос
        requestInDto = new ItemRequestInDto("Request of item");
        //создаем выходные запросы
        requestOutDto = new ItemRequestOutDto(
                requestId, "Request of item", LocalDateTime.now());
    }

    ///////////////////////////// Получение данных ///////////////////////////

    @Test
    void normalGetRequestTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(requestClient.getRequest(anyLong(), anyLong()))
                .thenReturn(makeResponse(requestOutDto));

        mvc.perform(get("/requests/{id}", requestId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestOutDto.getDescription())))
                .andExpect(jsonPath("$.created", is(convertTime(requestOutDto.getCreated()))));
    }

    @Test
    void getRequestWithNonPositiveRequestIdTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/requests/{id}", 0)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
        mvc.perform(get("/requests/{id}", -1)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getRequestWithNonPositiveUserIdTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "0");

        mvc.perform(get("/requests/{id}", requestId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
        headers.add(HEADER_NAME, "-1");
        mvc.perform(get("/requests/{id}", requestId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void normalGetRequestsByOwnerTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(requestClient.getRequestsByOwner(anyLong()))
                .thenReturn(makeResponse(List.of(requestOutDto)));

        mvc.perform(get("/requests")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestOutDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(convertTime(requestOutDto.getCreated()))));
    }

    @Test
    void getRequestsByOwnerWithNonPositiveOwnerIdTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "0");

        mvc.perform(get("/requests")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
        headers.add(HEADER_NAME, "-1");
        mvc.perform(get("/requests")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void normalGetAllRequestsTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(requestClient.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(requestOutDto)));

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestOutDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(convertTime(requestOutDto.getCreated()))));
    }

    @Test
    void getAllRequestsWithNonPositiveUserIdTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "0");

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
        headers.add(HEADER_NAME, "-1");
        mvc.perform(get("/requests/all")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getAllRequestsWithNegativeFromTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/requests/all?from={from}&size={size}", -1, 20)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllRequestsWithNonPositiveSizeTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, 0)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mvc.perform(get("/requests/all?from={from}&size={size}", 0, -1)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    ///////////////////////////// Создание запроса ///////////////////////////

    @Test
    void normalCreateRequestTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(requestClient.createRequest(anyLong(), any(ItemRequestInDto.class)))
                .thenReturn(makeResponse(requestOutDto));

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestOutDto.getDescription())))
                .andExpect(jsonPath("$.created", is(convertTime(requestOutDto.getCreated()))));
    }

    @Test
    void createRequestWithNonPositiveRequesterIdTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "0");
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
        headers.set(HEADER_NAME, "-1");
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    /////////////////////////// Формирование ответа //////////////////////////

    private ResponseEntity<Object> makeResponse(Object objectInDto) {
        return ResponseEntity.ok(objectInDto);
    }

    private ResponseEntity<Object> makeResponse(List<Object> objects) {
        return ResponseEntity.ok(objects);
    }

    ////////////////////////////// Формат времени ////////////////////////////

    private String convertTime(LocalDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(dateTime);
    }
}
