package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    ////////////////////////////////// CRUD //////////////////////////////////

    @Override
    public UserDto getUserById(long id) {
        return UserDtoMapper.toUserDto(userStorage.getById(id).orElseThrow(
                () -> new BadRequestException("Пользователь с идентификатором " + id + " не найден."))
        );
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получен список всех пользователей");
        return UserDtoMapper.listToUserDto(userStorage.getAll());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        String email = userDto.getEmail();
        if (userStorage.containsEmail(email)) { //пользователь уже есть
            throw new ConflictException("Запрошенный адрес " + email + " уже используется.");
        }
        User user = UserDtoMapper.toUser(userDto);
        userStorage.create(user);
        log.info("Создан новый пользователь с идентификатором " + user.getId());
        return UserDtoMapper.toUserDto(user);
    }

    @Override
    public UserDto patchUser(long id, UserDto userDto) {
        //читаем старого пользователя с заданным идентификатором (если он есть)
        User oldUser = userStorage.getById(id).orElseThrow(
                () -> new BadRequestException("Пользователь с идентификатором " + id + " не найден.")
        );
        //проверка корректности патча
        String email = userDto.getEmail();
        if (!oldUser.getEmail().equals(email) && userStorage.containsEmail(email)) {
            throw new ConflictException("Запрошенный адрес " + email + " уже используется.");
        }
        //установка имени
        String name = userDto.getName();
        if ((name != null) && !name.isBlank()) {
            oldUser.setName(name);
        }
        //установка email
        if (validateEmail(email)) { //некорректный email не устанавливаем
            oldUser.setEmail(email);
        }
        userStorage.update(oldUser);
        log.info("Обновлен пользователь с идентификатором " + oldUser.getId());
        return UserDtoMapper.toUserDto(oldUser);
    }

    @Override
    public void deleteUser(long id) {
        if (userStorage.delete(id)) {
            log.info("Удален пользователь с идентификатором " + id);
        } else {
            log.warn("Пользователь с идентификатором " + id + "не найден.");
        }
    }

    @Override
    public void deleteAllUsers() {
        int count = userStorage.deleteAll();
        log.info("Удалено " + count + " пользователей.");
    }

    //////////////////////////////// Валидация ///////////////////////////////

    //проверка корректности email
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
}
