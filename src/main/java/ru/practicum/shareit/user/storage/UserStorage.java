package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    boolean containsId(Long id);

    boolean containsEmail(String email);

    Optional<User> getById(long id);

    List<User> getAll();

    void create(User user);

    void update(User user);

    boolean delete(long id);

    int deleteAll();
}
