package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserInDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerMvcTests {
    private final long userId = 1L;
    private final UserInDto vasya = new UserInDto("Vasya", "vasya@com");
    private final UserInDto petya = new UserInDto("Petya", "petya@com");

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserClient userClient;

    @Autowired
    private MockMvc mvc;

    //////////////////////////// Получение данных ////////////////////////////

    @Test
    void getUserTest() throws Exception {
        when(userClient.getUser(anyLong()))
                .thenReturn(makeResponse(vasya));

        mvc.perform(get("/users/{id}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(vasya.getName())));
    }

    @Test
    void getUserWithNonPositiveIdTest() throws Exception {
        mvc.perform(get("/users/{id}", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
        mvc.perform(get("/users/{id}", -1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getAllUsersTest() throws Exception {
        when(userClient.getAllUsers())
                .thenReturn(makeResponse(List.of(vasya, petya)));

        mvc.perform(get("/users", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(vasya.getName())))
                .andExpect(jsonPath("$[0].email", is(vasya.getEmail())))
                .andExpect(jsonPath("$[1].name", is(petya.getName())))
                .andExpect(jsonPath("$[1].email", is(petya.getEmail())));

    }

    /////////////////////////// Создание и обновление ////////////////////////

    @Test
    void createUserWithBlancEmailTest() throws Exception {
        //посылаем пользователя с пустым email
        vasya.setEmail(" ");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        //посылаем пользователя с отсутствующим email
        vasya.setEmail(null);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void createUserWithBadEmailFormatTest() throws Exception {
        vasya.setEmail("@com");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        vasya.setEmail("vasya@");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        vasya.setEmail("vasya@ com");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void createNormalUserTest() throws Exception {
        when(userClient.createUser(any(UserInDto.class)))
                .thenReturn(makeResponse(vasya));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(vasya.getName())))
                .andExpect(jsonPath("$.email", is(vasya.getEmail())));
    }

    @Test
    void patchUserWithNonPositiveIdTest() throws Exception {
        mvc.perform(patch("/users/{id}", 0)
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mvc.perform(patch("/users/{id}", -1)
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void patchUserWithBadEmailOnlyTest() throws Exception {
        vasya.setEmail("vasya@");
        when(userClient.patchUser(anyLong(), any(UserInDto.class)))
                .thenAnswer(invocationOnMock -> {
                    UserInDto user = invocationOnMock.getArgument(1, UserInDto.class);
                    return makeResponse(user);
                });

        mvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(vasya.getName())))
                .andExpect(jsonPath("$.email", is(""))); //неверный email обнуляется
    }

    @Test
    void patchNormalUserTest() throws Exception {
        when(userClient.patchUser(anyLong(), any(UserInDto.class)))
                .thenAnswer(invocationOnMock -> {
                    UserInDto user = invocationOnMock.getArgument(1, UserInDto.class);
                    return makeResponse(user);
                });

        mvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(vasya))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(vasya.getName())))
                .andExpect(jsonPath("$.email", is(vasya.getEmail())));
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @Test
    void deleteUserByIdTest() throws Exception {
        mvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAllUsersTest() throws Exception {
        mvc.perform(delete("/users"))
                .andExpect(status().isOk());
    }

    /////////////////////////// Формирование ответа //////////////////////////

    private ResponseEntity<Object> makeResponse(UserInDto userInDto) {
        return ResponseEntity.ok(userInDto);
    }

    private ResponseEntity<Object> makeResponse(List<UserInDto> users) {
        return ResponseEntity.ok(users);
    }
}
