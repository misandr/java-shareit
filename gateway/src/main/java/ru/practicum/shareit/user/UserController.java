package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> addUser(@RequestBody @Valid User user) {
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

        return userClient.addUser(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @Valid @RequestBody User user) {
        log.info("Change user {}", userId, user);

        return userClient.updateUser(userId, user);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Get list users.");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable Long userId) {
        log.info("Get user by id {}", userId);
        return userClient.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("Delete user with id {}", userId);
        return userClient.deleteUser(userId);
    }
}
