package ru.practicum.shareit.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех пользователей
    @GetMapping
    public List<UserDto> getAllUsers() {
        return service.getAllUsers();
    }

    //получение пользователя по идентификатору
    @GetMapping(value = "/{id}")
    public UserDto getUser(@PathVariable("id") long userId) {
        return service.getUserById(userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return service.createUser(userDto); //здесь поля проверяются автоматически
    }

    @PatchMapping(value = "/{id}")
    public UserDto patchUser(@PathVariable("id") long userId, @RequestBody UserDto userDto) {
        return service.patchUser(userId, userDto); //здесь поля проверяются в service
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    @DeleteMapping(value = "/{id}")
    public void deleteUser(@PathVariable long id) {
        service.deleteUser(id);
    }

    @DeleteMapping
    public void deleteAllUsers() {
        service.deleteAllUsers();
    }
}
