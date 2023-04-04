package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Add new user {}", user);

        return userService.addUser(user);
    }

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable Long userId, @RequestBody User user) {
        log.info("Change user {}", user);

        user.setId(userId);
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Get list users.");

        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable Long userId) {
        log.info("Get user by id {}", userId);

        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Delete user with id {}", userId);

        userService.deleteUser(userId);
    }
}
