package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserOutDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.dto.UserInDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {
    //private final UserStorage userStorage;
    private final UserRepository userRepository;

    ////////////////////////////////// CRUD //////////////////////////////////

    @Override
    public UserOutDto getUserById(long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с идентификатором " + id + " не найден.")
        );
        return UserDtoMapper.toUserOutDto(user);
    }

    @Override
    public List<UserOutDto> getAllUsers() {
        log.info("Получен список всех пользователей");
        return UserDtoMapper.listToUserDto(userRepository.findAll());
    }

    @Override
    public UserOutDto createUser(UserInDto userInDto) {
        String email = userInDto.getEmail();
        //if (userStorage.containsEmail(email)) { //пользователь уже есть
        //TODO ПРОВЕРИТЬ
        //TODO ?????????????????????????
        /*if (userRepository.findByEmail(email) != null) { //пользователь уже есть
            throw new ConflictException("Запрошенный адрес " + email + " уже используется.");
        }*/
        //TODO ?????????????????????????
        User user = UserDtoMapper.toUser(userInDto);
        //userStorage.create(user);
        //TODO ПРОВЕРИТЬ
        log.info("Создан новый пользователь с идентификатором " + user.getId());
        //TODO ??????????????????????????
        User newUser = userRepository.save(user);
        return UserDtoMapper.toUserOutDto(newUser);
        //return UserDtoMapper.toUserOutDto(userRepository.save(user));
    }

    @Override
    public UserOutDto patchUser(long id, UserInDto userInDto) {
        //читаем старого пользователя с заданным идентификатором (если он есть)
        User oldUser = userRepository.findById(id).orElseThrow(
                () -> new BadRequestException("Пользователь с идентификатором " + id + " не найден.")
        );
        //проверка корректности патча
        String email = userInDto.getEmail();
        if (!oldUser.getEmail().equals(email) && (userRepository.findByEmail(email) != null)) {
            throw new ConflictException("Запрошенный адрес " + email + " уже используется.");
        }
        //установка имени
        String name = userInDto.getName();
        if ((name != null) && !name.isBlank()) {
            oldUser.setName(name);
        }
        //установка email
        if (validateEmail(email)) { //некорректный email не устанавливаем
            oldUser.setEmail(email);
        }
        //userStorage.update(oldUser);
        //TODO ПРОВЕРИТЬ
        User user = userRepository.save(oldUser);
        log.info("Обновлен пользователь с идентификатором " + oldUser.getId());
        return UserDtoMapper.toUserOutDto(user);
    }

    @Override
    public void deleteUser(long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("Удален пользователь с идентификатором " + id);
        } else {
            log.warn("Пользователь с идентификатором " + id + "не найден.");
        }
    }

    @Override
    public void deleteAllUsers() {
        //int count = userStorage.deleteAll();
        long count = userRepository.count(); //число пользователей
        userRepository.deleteAll(); //удаляем всех
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
