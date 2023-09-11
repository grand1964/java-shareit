package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerMvcTest {
    private static final String HEADER_NAME = "X-Sharer-User-Id";
    private static final long itemId = 1L;
    private static final long requestId = 1L;
    private static ItemInDto itemInDto;
    private static ItemInDto patchInDto;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemClient itemClient;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        //создаем входные объекты-вещи
        itemInDto = new ItemInDto(itemId, "notebook", "ASUS", true, requestId);
        patchInDto = new ItemInDto(itemId, "smartphone", "Samsung", true, requestId);
    }

    ///////////////////////////// Получение данных ///////////////////////////

    @Test
    void getNormalItemTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", "1");
        when(itemClient.getItem(anyLong(), anyLong()))
                .thenReturn(makeResponse(itemInDto));

        mvc.perform(get("/items/{id}", itemId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemInDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemInDto.getName())))
                .andExpect(jsonPath("$.description", is(itemInDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemInDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(itemInDto.getRequestId()), Long.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void getItemWithNonPositiveItemIdTest(long value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        mvc.perform(get("/items/{id}", value)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void normalGetAllItemsTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(itemClient.getAllItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(itemInDto, patchInDto)));

        mvc.perform(get("/items")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemInDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemInDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemInDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemInDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].requestId", is(itemInDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(patchInDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(patchInDto.getName())))
                .andExpect(jsonPath("$[1].description", is(patchInDto.getDescription())))
                .andExpect(jsonPath("$[1].available", is(patchInDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[1].requestId", is(patchInDto.getRequestId()), Long.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void getAllItemsWithNonPositiveOwnerTest(String value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, value);
        mvc.perform(get("/items")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getAllItemsWithNonPositiveSizeTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        mvc.perform(get("/items?from=0&size=0")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        headers.set(HEADER_NAME, "-1");
        mvc.perform(get("/items?from=0&size=-1")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllItemsWithNegativeFromTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        mvc.perform(get("/items?from=0&size=0")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        headers.set(HEADER_NAME, "-1");
        mvc.perform(get("/items?from=-1&size=20")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @Test
    void normalCreateItemTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(itemClient.createItem(anyLong(), any(ItemInDto.class)))
                .thenReturn(makeResponse(itemInDto));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemInDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemInDto.getName())))
                .andExpect(jsonPath("$.description", is(itemInDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemInDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(itemInDto.getRequestId()), Long.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void createItemWithNonPositiveOwnerIdTest(String value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, value);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void normalPatchItemTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        when(itemClient.patchItem(anyLong(), anyLong(), any(ItemInDto.class)))
                .thenAnswer(invocationOnMock -> {
                    ItemInDto item = invocationOnMock.getArgument(2, ItemInDto.class);
                    return makeResponse(item);
                });

        mvc.perform(patch("/items/{id}", 10L)
                        .content(mapper.writeValueAsString(patchInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10L), Long.class))
                .andExpect(jsonPath("$.name", is(patchInDto.getName())))
                .andExpect(jsonPath("$.description", is(patchInDto.getDescription())))
                .andExpect(jsonPath("$.available", is(patchInDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(patchInDto.getRequestId()), Long.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void patchItemWithNonPositiveItemIdTest(long value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        mvc.perform(patch("/items/{id}", value)
                        .content(mapper.writeValueAsString(patchInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void patchItemWithNonPositiveOwnerIdTest(String value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, value);
        mvc.perform(patch("/items/{id}", 1)
                        .content(mapper.writeValueAsString(patchInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void normalCreateCommentTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        CommentInDto commentInDto = new CommentInDto("Comment");
        when(itemClient.createComment(anyLong(), anyLong(), any(CommentInDto.class)))
                .thenReturn(makeResponse(commentInDto));

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentInDto.getText())));
    }

    @Test
    void createCommentWithBlancTextTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        CommentInDto commentInDto = new CommentInDto(" ");

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        commentInDto.setText(null);
        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void createCommentWithNonPositiveAuthorIdTest(String value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, value);
        CommentInDto commentInDto = new CommentInDto("Comment");

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void createCommentWithNonPositiveItemIdTest(long value) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, "1");
        CommentInDto commentInDto = new CommentInDto("Comment");

        mvc.perform(post("/items/{itemId}/comment", value)
                        .content(mapper.writeValueAsString(commentInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @Test
    void deleteItemByIdTest() throws Exception {
        mvc.perform(delete("/items/{id}", itemId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAllItemsTest() throws Exception {
        mvc.perform(delete("/items"))
                .andExpect(status().isOk());
    }

    ////////////////////////////////// Поиск /////////////////////////////////

    @Test
    void normalSearchItemsTest() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(itemInDto, patchInDto)));

        mvc.perform(get("/items/search?text=abc&from=0&size=20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemInDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemInDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemInDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemInDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[1].id", is(patchInDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(patchInDto.getName())))
                .andExpect(jsonPath("$[1].description", is(patchInDto.getDescription())))
                .andExpect(jsonPath("$[1].available", is(patchInDto.getAvailable()), Boolean.class));
    }

    @Test
    void searchItemsWithNegativeFromTest() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(itemInDto, patchInDto)));

        mvc.perform(get("/items/search?text=abc&from=-1&size=20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void searchItemsWithNonPositiveSizeTest() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(itemInDto, patchInDto)));

        mvc.perform(get("/items/search?text=abc&from=0&size=0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mvc.perform(get("/items/search?text=abc&from=0&size=-1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    /////////////////////////// Формирование ответа //////////////////////////

    private ResponseEntity<Object> makeResponse(Object objectInDto) {
        return ResponseEntity.ok(objectInDto);
    }

    private ResponseEntity<Object> makeResponse(List<Object> objects) {
        return ResponseEntity.ok(objects);
    }
}
