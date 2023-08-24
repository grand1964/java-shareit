package ru.practicum.shareit.integral.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.user.dto.UserOutDto;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserTests {
    private final String nameV = "Vasya";
    private final String nameP = "Petya";
    private final String emailV = "vasya@com";
    private final String emailP = "petya@com";

    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update(TestUtils.getSqlForResetUsers());
    }

    @Test
    public void createTwoUsersWithDifferentEmailsTest() {
        userService.createUser(TestUtils.createUserInDto(nameV, emailV));
        userService.createUser(TestUtils.createUserInDto(nameP, emailP));
        List<UserOutDto> users = userService.getAllUsers();
        assertEquals(users.size(), 2);
        assertEquals(users.get(0).getName(), nameV);
        assertEquals(users.get(1).getEmail(), emailP);
    }

    @Test
    public void updateWithBothParamsTest() {
        userService.createUser(TestUtils.createUserInDto(nameV, emailV));
        userService.patchUser(1, TestUtils.createUserInDto(nameP, emailP));
        List<UserOutDto> users = userService.getAllUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getName(), nameP);
        assertEquals(users.get(0).getEmail(), emailP);
    }

    @Test
    public void deleteTest() {
        userService.createUser(TestUtils.createUserInDto(nameV, emailV));
        userService.createUser(TestUtils.createUserInDto(nameP, emailP));
        userService.deleteUser(2);
        userService.createUser(TestUtils.createUserInDto("Fedya", "fedya@com"));
        List<UserOutDto> users = userService.getAllUsers();
        assertEquals(users.size(), 2);
        assertEquals(users.get(0).getId(), 1);
        assertEquals(users.get(0).getName(), "Vasya");
        assertEquals(users.get(1).getId(), 3);
        assertEquals(users.get(1).getEmail(), "fedya@com");
    }

    @Test
    public void deleteAllTest() {
        userService.createUser(TestUtils.createUserInDto(nameV, emailV));
        userService.createUser(TestUtils.createUserInDto(nameP, emailP));
        userService.deleteAllUsers();
        List<UserOutDto> users = userService.getAllUsers();
        assertEquals(users.size(), 0);
    }
}
