package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserInDto;
import ru.practicum.shareit.user.dto.UserOutDto;
import ru.practicum.shareit.user.service.UserService;

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
public class UserControllerMvcTest {
    private final long userId = 1L;
    private final UserInDto userInDto = new UserInDto("Vasya", "vasya@com");
    private final UserInDto patchInDto = new UserInDto("Petya", "petya@com");
    private final UserOutDto userOutDto = new UserOutDto(userId, "Vasya", "vasya@com");
    private final UserOutDto patchOutDto = new UserOutDto(userId, "Petya", "petya@com");

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    ///////////////////////////// Получение данных ///////////////////////////

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenReturn(userOutDto);

        mvc.perform(get("/users/{id}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userOutDto.getName())))
                .andExpect(jsonPath("$.email", is(userOutDto.getEmail())));
    }

    @Test
    void getAllUsersTest() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of(userOutDto, patchOutDto));

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(userOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userOutDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userOutDto.getEmail())))
                .andExpect(jsonPath("$[1].id", is(patchOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(patchOutDto.getName())))
                .andExpect(jsonPath("$[1].email", is(patchOutDto.getEmail())));
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @Test
    void createUserTest() throws Exception {
        when(userService.createUser(any(UserInDto.class)))
                .thenReturn(userOutDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userInDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userOutDto.getName())))
                .andExpect(jsonPath("$.email", is(userOutDto.getEmail())));
    }

    @Test
    void patchUserTest() throws Exception {
        when(userService.patchUser(anyLong(), any(UserInDto.class)))
                .thenReturn(patchOutDto);
        mvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(patchInDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(patchOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(patchOutDto.getName())))
                .andExpect(jsonPath("$.email", is(patchOutDto.getEmail())));
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
}
