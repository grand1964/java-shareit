package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserTests {
    private final UserService userService;

    @BeforeEach
    public void resetUsers() {
        userService.deleteAllUsers();
    }

    @Test
    public void createWithDuplicatedEmailTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        UserDto dto = TestUtils.createUserDto("Petya", "vasya@com");
        assertThrows(ConflictException.class, () -> userService.createUser(dto));
    }

    @Test
    public void createWithDisjointEmailTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.createUser(TestUtils.createUserDto("Petya", "petya@com"));
        List<UserDto> users = userService.getAllUsers();
        assertEquals(users.size(), 2);
        assertEquals(users.get(0).getName(), "Vasya");
        assertEquals(users.get(1).getEmail(), "petya@com");
    }

    @Test
    public void updateWithBothParamsTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.patchUser(1, TestUtils.createUserDto("Petya", "petya@com"));
        List<UserDto> users = userService.getAllUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getName(), "Petya");
        assertEquals(users.get(0).getEmail(), "petya@com");
    }

    @Test
    public void updateWithNameOnlyTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.patchUser(1, TestUtils.createUserDto("Petya", null));
        List<UserDto> users = userService.getAllUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getName(), "Petya");
        assertEquals(users.get(0).getEmail(), "vasya@com");
    }

    @Test
    public void updateWithEmailOnlyTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.patchUser(1, TestUtils.createUserDto("   ", "petya@com"));
        List<UserDto> users = userService.getAllUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getName(), "Vasya");
        assertEquals(users.get(0).getEmail(), "petya@com");
    }

    @Test
    public void updateWithNameAndBadEmailTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.patchUser(1, TestUtils.createUserDto("Petya", "@com"));
        userService.patchUser(1, TestUtils.createUserDto("Petya", "petya@"));
        userService.patchUser(1, TestUtils.createUserDto("Petya", "petya_com"));
        userService.patchUser(1, TestUtils.createUserDto("Petya", "petya @com"));
        List<UserDto> users = userService.getAllUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getName(), "Petya");
        assertEquals(users.get(0).getEmail(), "vasya@com");
    }

    @Test
    public void deleteTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.createUser(TestUtils.createUserDto("Petya", "petya@com"));
        userService.deleteUser(2);
        userService.createUser(TestUtils.createUserDto("Fedya", "fedya@com"));
        List<UserDto> users = userService.getAllUsers();
        assertEquals(users.size(), 2);
        assertEquals(users.get(0).getId(), 1);
        assertEquals(users.get(0).getName(), "Vasya");
        assertEquals(users.get(1).getId(), 3);
        assertEquals(users.get(1).getEmail(), "fedya@com");
    }

    @Test
    public void deleteAllTest() {
        userService.createUser(TestUtils.createUserDto("Vasya", "vasya@com"));
        userService.createUser(TestUtils.createUserDto("Petya", "petya@com"));
        userService.deleteAllUsers();
        List<UserDto> users = userService.getAllUsers();
        assertEquals(users.size(), 0);
    }
}
