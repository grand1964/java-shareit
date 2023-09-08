package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.validation.Validation;
import ru.practicum.shareit.user.dto.UserInDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех пользователей
    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Запрошено получение всех пользователей.");
        return userClient.getAllUsers();
    }

    //получение пользователя по идентификатору
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getUser(@PathVariable("id") long userId) {
        Validation.validateId(userId);
        log.info("Запрошен пользователь с идентификатором {}", userId);
        return userClient.getUser(userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserInDto userInDto) {
        log.info("Запрошено создание нового пользователя {}", userInDto);
        return userClient.createUser(userInDto);
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<Object> patchUser(@PathVariable("id") @Positive long userId,
                                            @RequestBody UserInDto userInDto) {
        Validation.validateUserDto(userInDto); //здесь корректность email проверяется вручную
        log.info("Запрошено обновление пользователя с идентификатором {}", userId);
        return userClient.patchUser(userId, userInDto);
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable long id) {
        log.info("Запрошено удаление пользователя с идентификатором {}", id);
        return userClient.deleteUser(id);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteAllUsers() {
        log.info("Запрошено удаление всех пользователей.");
        return userClient.deleteAllUsers();
    }
}
