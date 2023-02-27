package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUser(Integer userId);

    void deleteUser(Integer userId);
}
