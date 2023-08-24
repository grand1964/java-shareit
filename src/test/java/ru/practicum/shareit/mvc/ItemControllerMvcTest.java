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
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserOutDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private static HttpHeaders headers;
    private static final long itemId = 1L;
    private static final long userId = 1L;
    private static final long requestId = 1L;
    private static ItemInDto itemInDto;
    private static ItemInDto patchInDto;
    private static ItemOutDto itemOutDto;
    private static ItemOutDto patchOutDto;
    private static ItemOutBookedDto itemOutBookedDto;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @MockBean
    CommentService commentService;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        //создаем дополнительный заголовок
        headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", "1");
        //создаем пользователя
        UserOutDto booker = new UserOutDto(userId, "vasya", "vasya@com");
        //создаем входные объекты-вещи
        itemInDto = new ItemInDto(itemId, "notebook", "ASUS", true, requestId);
        patchInDto = new ItemInDto(itemId, "smartphone", "Samsung", true, requestId);
        //создаем простые выходные объекты
        itemOutDto = new ItemOutDto(
                itemId, "notebook", "ASUS", true, requestId, new ArrayList<>());
        patchOutDto = new ItemOutDto(
                itemId, "smartphone", "Samsung", true, requestId, new ArrayList<>());
        //создаем бронирования
        BookingOutDto lastBooking = new BookingOutDto(itemId, LocalDateTime.now(),
                LocalDateTime.now().plusHours(1), Status.APPROVED, booker, itemOutDto, userId, itemId);
        BookingOutDto nextBooking = new BookingOutDto(itemId, LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(3), Status.APPROVED, booker, itemOutDto, userId, itemId);
        //создаем комментарии
        List<CommentOutDto> comments = new ArrayList<>();
        comments.add(new CommentOutDto(1L, "Comment1", "Vasya", LocalDateTime.now()));
        comments.add(new CommentOutDto(2L, "Comment2", "Petya", LocalDateTime.now().plusHours(1)));
        //создаем выходной объект с датами бронирования
        itemOutBookedDto = new ItemOutBookedDto(itemId, "notebook",
                "ASUS", true, requestId, lastBooking, nextBooking, comments);
    }

    ///////////////////////////// Получение данных ///////////////////////////

    @Test
    void getItemTest() throws Exception {
        when(itemService.getItem(anyLong(), anyLong()))
                .thenReturn(itemOutBookedDto);

        mvc.perform(get("/items/{id}", itemId)
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutBookedDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemOutBookedDto.getName())))
                .andExpect(jsonPath("$.description", is(itemOutBookedDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutBookedDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(itemOutBookedDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments[0].authorName", is("Vasya")))
                .andExpect(jsonPath("$.comments[0].id", is(1)))
                .andExpect(jsonPath("$.comments[1].text", is("Comment2")));
    }

    @Test
    void getAllItemsTest() throws Exception {
        when(itemService.getAllItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemOutBookedDto));

        mvc.perform(get("/items")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemOutBookedDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemOutBookedDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemOutBookedDto.getDescription())))
                .andExpect(jsonPath("$[0].available",
                        is(itemOutBookedDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[0].requestId", is(itemOutBookedDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].comments", hasSize(2)))
                .andExpect(jsonPath("$[0].comments[0].authorName", is("Vasya")))
                .andExpect(jsonPath("$[0].comments[0].id", is(1)))
                .andExpect(jsonPath("$[0].comments[1].text", is("Comment2")));
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @Test
    void createItemTest() throws Exception {
        when(itemService.createItem(anyLong(), any(ItemInDto.class)))
                .thenReturn(itemOutDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$.description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(itemOutDto.getRequestId()), Long.class));
    }

    @Test
    void patchItemTest() throws Exception {
        when(itemService.patchItem(anyLong(), any(ItemInDto.class)))
                .thenReturn(patchOutDto);

        mvc.perform(patch("/items/{id}", userId)
                        .content(mapper.writeValueAsString(patchInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(patchOutDto.getName())))
                .andExpect(jsonPath("$.description", is(patchOutDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(itemOutDto.getRequestId()), Long.class));
    }

    @Test
    void createCommentTest() throws Exception {
        CommentInDto commentInDto = new CommentInDto("Comment");
        CommentOutDto commentOutDto = new CommentOutDto(
                1L, "Comment", "Vasya", LocalDateTime.now());
        when(commentService.createComment(anyLong(), anyLong(), any(CommentInDto.class)))
                .thenReturn(commentOutDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(commentInDto))
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentOutDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentOutDto.getAuthorName())));
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @Test
    void deleteItemByIdTest() throws Exception {
        mvc.perform(delete("/items/{id}", itemId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAllItemsTest() throws Exception {
        mvc.perform(delete("/items/{id}", itemId))
                .andExpect(status().isOk());
    }

    ////////////////////////////////// Поиск /////////////////////////////////

    @Test
    void searchItemsTest() throws Exception {
        when(itemService.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(itemOutDto));

        mvc.perform(get("/items/search")
                        .headers(headers)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemOutDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemOutDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemOutDto.getAvailable()), Boolean.class));
    }
}
