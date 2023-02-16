package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;

import ru.practicum.shareit.exceptions.ValidationException;

import java.util.*;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private int generateId;
    private final Map<Integer, User> users;

    public InMemoryUserStorage() {
        generateId = 1;
        users = new HashMap<>();
    }

    @Override
    public User addUser(User user) {

        if (user == null) {
            log.warn("Запрос пустой!");
            throw new ValidationException("Запрос пустой!");
        }

        if ((user.getName() == null) || user.getName().isBlank()) {
            log.warn("Имя неправлиьное!");
            throw new ValidationException("Имя неправлиьное!");
        }

        if (user.getEmail() == null) {
            log.warn("Почта не указана!");
            throw new ValidationException("Почта не указана!");
        }

        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Почта неправильная!");
            throw new ValidationException("Почта неправильная!");
        }

        if (existEmail(user.getEmail())) {
            log.warn("Почта уже существует!");
            throw new ConflictException("Почта уже существует!");
        }

        user.setId(generateId);
        users.put(generateId, user);

        generateId++;
        return user;
    }

    @Override
    public User updateUser(User user) {
        User findedUser = getUser(user.getId());
        if (findedUser != null) {
            if (user.getEmail() != null) {
                if (existEmail(user.getEmail())) {
                    log.warn("Почта уже существует!");
                } else {
                    findedUser.setEmail(user.getEmail());
                }
            }
            if (user.getName() != null) {
                findedUser.setName(user.getName());
            }
        } else {
            throw new NotFoundException("Нет такого пользователя!");
        }
        return findedUser;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(Integer userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            log.warn("Нет такого пользователя!");
            throw new NotFoundException("Нет такого пользователя!");
        }
    }

    @Override
    public void deleteUser(Integer userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);
        } else {
            log.warn("Нет такого пользователя!");
            throw new NotFoundException("Нет такого пользователя!");
        }
    }

    private boolean existEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email))
                return true;
        }

        return false;
    }
}
