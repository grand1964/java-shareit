package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserInDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
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
        validateId(userId);
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
        //если email некорректный, то заменяем его пустым
        if (!validateEmail(userInDto.getEmail())) {
            userInDto.setEmail("");
        }
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

    ////////////////////////////// Валидация email ///////////////////////////

    private boolean validateEmail(String email) {
        if ((email == null) || (email.isBlank())) {
            return false; //адрес должен быть непустым
        }
        int pos = email.indexOf('@');
        if ((pos < 1) || (pos == email.length() - 1)) {
            return false; //@ должно быть не первым и не последним символом
        }
        return (email.indexOf(' ') == -1); //не должно быть пробелов
    }

    ///////////////////////// Валидация идентификаторов ///////////////////////

    private void validateId(long... ids) {
        for (long id : ids) {
            if (id <= 0) {
                throw new NotFoundException("Идентификатор должен быть положительным");
            }
        }
    }
}
