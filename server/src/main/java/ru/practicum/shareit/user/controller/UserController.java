package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserInDto;
import ru.practicum.shareit.user.dto.UserOutDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class UserController {
    private final UserService service;

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех пользователей
    @GetMapping
    public List<UserOutDto> getAllUsers() {
        log.info("Запрошено получение всех пользователей.");
        return service.getAllUsers();
    }

    //получение пользователя по идентификатору
    @GetMapping(value = "/{id}")
    public UserOutDto getUser(@PathVariable("id") long userId) {
        log.info("Запрошен пользователь с идентификатором " + userId);
        return service.getUserById(userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @PostMapping
    public UserOutDto createUser(@RequestBody UserInDto userInDto) {
        log.info("Запрошено создание нового пользователя.");
        return service.createUser(userInDto);
    }

    @PatchMapping(value = "/{id}")
    public UserOutDto patchUser(@PathVariable("id") long userId, @RequestBody UserInDto userInDto) {
        log.info("Запрошено обновление пользователя с идентификатором " + userId);
        return service.patchUser(userId, userInDto); //здесь поля проверяются в service
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @DeleteMapping(value = "/{id}")
    public void deleteUser(@PathVariable long id) {
        log.info("Запрошено удаление пользователя с идентификатором " + id);
        service.deleteUser(id);
    }

    @DeleteMapping
    public void deleteAllUsers() {
        log.info("Запрошено удаление всех пользователей.");
        service.deleteAllUsers();
    }
}
