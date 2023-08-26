package ru.practicum.shareit.junit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserOutDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserTests {
    private final long id = 1L;
    private final String name = "Vasya"; //текущее имя
    private final String email = "vasya@com"; //текущий email
    UserService userService;
    @Mock
    UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
        //метод findById возвращает конкретного пользователя с заданным id, name и email
        Mockito
                .when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(TestUtils.createUser(id, name, email)));
        //метод save возвращает того же пользователя с предписанным id
        Mockito
                .when(userRepository.save(any(User.class)))
                .thenAnswer(invocationOnMock -> {
                    User user = invocationOnMock.getArgument(0, User.class);
                    user.setId(id);
                    return user;
                });
    }

    ///////////////////////////// Тесты создания /////////////////////////////

    @Test
    public void createNewUserTest() {
        //создаем требуемого пользователя
        userService.createUser(TestUtils.createUserDto(name, email));
        //проверяем результат
        UserOutDto newUserDto = userService.getUserById(id);
        assertEquals(newUserDto.getId(), 1);
        assertEquals(newUserDto.getName(), "Vasya");
        assertEquals(newUserDto.getEmail(), "vasya@com");
        //метод findAll возвращает список пользователей
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(new User(id, name, email)));
        List<UserOutDto> users = userService.getAllUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getId(), id);
        assertEquals(users.get(0).getName(), name);
        assertEquals(users.get(0).getEmail(), email);
        //сохранение - 1 раз
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    //////////////////////////// Тесты обновления ////////////////////////////

    @Test
    public void updateWithNameAndEmailTest() {
        String newName = "Petya"; //новое имя
        String newEmail = "petya@com"; //новый email
        //метод findByEmail возвращает пустого пользователя с новым email
        Mockito
                .when(userRepository.findByEmail(newEmail))
                .thenReturn(null);
        //создаем требуемого пользователя
        userService.createUser(TestUtils.createUserDto(name, email));
        //обновляем его пользователем без email
        UserOutDto userOutDto = userService.patchUser(1, TestUtils.createUserDto(newName, newEmail));
        //проверяем результат
        assertEquals(userOutDto.getId(), 1);
        assertEquals(userOutDto.getName(), "Petya");
        assertEquals(userOutDto.getEmail(), "petya@com");
        //сохранение - 2 раза (create и patch)
        Mockito.verify(userRepository, Mockito.times(2)).save(any(User.class));
    }

    @Test
    public void updateWithNameOnlyTest() {
        String newName = "Petya"; //новое имя
        //метод findByEmail возвращает пустого пользователя с новым email
        Mockito
                .when(userRepository.findByEmail(null))
                .thenReturn(null);
        //метод save возвращает того же пользователя с id = 1
        //создаем требуемого пользователя
        userService.createUser(TestUtils.createUserDto(name, email));
        //обновляем его пользователем без email
        UserOutDto userOutDto = userService.patchUser(1, TestUtils.createUserDto(newName, null));
        //проверяем результат
        assertEquals(userOutDto.getId(), 1);
        assertEquals(userOutDto.getName(), "Petya");
        assertEquals(userOutDto.getEmail(), "vasya@com");
        //сохранение - 2 раза (create и patch)
        Mockito.verify(userRepository, Mockito.times(2)).save(any(User.class));
    }

    @Test
    public void updateWithEmailOnlyTest() {
        String newName = "  "; //новое имя пустое
        String newEmail = "petya@com"; //новый email
        //метод findByEmail возвращает пустого пользователя с новым email
        Mockito
                .when(userRepository.findByEmail(newEmail))
                .thenReturn(null);
        //создаем требуемого пользователя
        userService.createUser(TestUtils.createUserDto(name, email));
        //обновляем его пользователем без email
        UserOutDto userOutDto = userService.patchUser(1, TestUtils.createUserDto(newName, newEmail));
        //проверяем результат
        assertEquals(userOutDto.getId(), 1);
        assertEquals(userOutDto.getName(), "Vasya");
        assertEquals(userOutDto.getEmail(), "petya@com");
        //сохранение - 2 раза (create и patch)
        Mockito.verify(userRepository, Mockito.times(2)).save(any(User.class));
    }

    @Test
    public void updateWithNameAndBadEmailTest() {
        String newName = "Petya"; //новое имя
        //метод findByEmail возвращает пустого пользователя с любым email
        Mockito
                .when(userRepository.findByEmail(anyString()))
                .thenReturn(null);
        //создаем требуемого пользователя
        userService.createUser(TestUtils.createUserDto(name, email));
        //обновляем его пользователем с некорректным email
        UserOutDto userOutDto = userService.patchUser(1, TestUtils.createUserDto(newName, "@com"));
        assertEquals(userOutDto.getEmail(), "vasya@com");
        userOutDto = userService.patchUser(1, TestUtils.createUserDto(newName, "petya@"));
        assertEquals(userOutDto.getEmail(), "vasya@com");
        userOutDto = userService.patchUser(1, TestUtils.createUserDto(newName, "petya_com"));
        assertEquals(userOutDto.getEmail(), "vasya@com");
        userOutDto = userService.patchUser(1, TestUtils.createUserDto(newName, "petya @com"));
        assertEquals(userOutDto.getEmail(), "vasya@com");
        //сохранение - 5 раз (create и 4 patch)
        Mockito.verify(userRepository, Mockito.times(5)).save(any(User.class));
    }
}
