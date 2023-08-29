package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class UserJpaTests {
    private static User userVasya;
    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository repository;

    @BeforeAll
    public static void setUp() {
        userVasya = new User(null, "Vasya", "vasya@com");
    }

    @BeforeEach
    public void clearAll() {
        repository.deleteAll();
    }

    @Test
    public void duplicatedEmailTest() {
        User badUser = new User(null, "Petya", "vasya@com");
        repository.save(userVasya);
        assertThrows(DataIntegrityViolationException.class, () -> repository.save(badUser));
    }

    @Test
    public void findByEmailTest() {
        User userPetya = new User(null, "Petya", "petya@com");
        repository.save(userVasya);
        repository.save(userPetya);
        User user = repository.findByEmail("vasya@com");
        assertEquals(user.getName(), "Vasya");
    }
}
