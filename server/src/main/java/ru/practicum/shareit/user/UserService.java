package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    User addUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUser(Long userId);

    void deleteUser(Long userId);
}
