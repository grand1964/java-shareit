package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.dto.UserInDto;
import ru.practicum.shareit.user.dto.UserOutDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {
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
        User user = UserDtoMapper.toUser(userInDto);
        User newUser = userRepository.save(user);
        log.info("Создан новый пользователь с идентификатором " + newUser.getId());
        return UserDtoMapper.toUserOutDto(newUser);
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
        if (!email.isBlank()) { //некорректный email заменяется в gateway пустым
            oldUser.setEmail(email);
        }
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
        long count = userRepository.count(); //число пользователей
        userRepository.deleteAll(); //удаляем всех
        log.info("Удалено " + count + " пользователей.");
    }
}
