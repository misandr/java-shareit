package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ConflictException;

import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
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
            log.warn("Request for add user is empty!");
            throw new NullValidationException("User");
        }

        if ((user.getName() == null) || user.getName().isBlank()) {
            log.warn("Bad name for user!");
            throw new ValidationException("Bad name for user!");
        }

        if (user.getEmail() == null) {
            log.warn("Email is null!");
            throw new NullValidationException("Email");
        }

        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Bad email!");
            throw new ValidationException("Bad email!");
        }

        if (existEmail(user.getEmail())) {
            log.warn("Email exists!");
            throw new ConflictException("Email exists!");
        }

        user.setId(generateId);
        users.put(generateId, user);

        generateId++;
        return user;
    }

    @Override
    public User updateUser(User user) {
        User gettedUser = getUser(user.getId());

        if (user.getEmail() != null) {
            if (!gettedUser.getEmail().equals(user.getEmail())) {
                if (existEmail(user.getEmail())) {
                    log.warn("Email exists!");
                    throw new ConflictException("Email exists!");
                }
            }
            gettedUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            gettedUser.setName(user.getName());
        }

        return gettedUser;
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
            log.warn("Not found user " + userId);
            throw new UserNotFoundException(userId);
        }
    }

    @Override
    public void deleteUser(Integer userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);
        } else {
            log.warn("Not found user " + userId);
            throw new UserNotFoundException(userId);
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
