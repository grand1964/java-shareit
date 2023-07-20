package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserStorageImpl implements UserStorage {
    private long currentId;
    private final Map<Long, User> users;

    public UserStorageImpl() {
        currentId = 0;
        users = new HashMap<>();
    }

    @Override
    public boolean containsId(Long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean containsEmail(String email) {
        if (email == null) {
            return false;
        } else {
            return users.values().stream().anyMatch(u -> email.equals(u.getEmail()));
        }
    }

    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return users.values().stream()
                .sorted(Comparator.comparingLong(User::getId))
                .collect(Collectors.toList());
    }

    @Override
    public void create(User user) {
        long id = getId();
        user.setId(id);
        users.put(id, user);
    }

    @Override
    public void update(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public boolean delete(long id) {
        if (containsId(id)) {
            users.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int deleteAll() {
        int size = users.size();
        users.clear();
        currentId = 0;
        return size;
    }

    private long getId() {
        return ++currentId;
    }
}
