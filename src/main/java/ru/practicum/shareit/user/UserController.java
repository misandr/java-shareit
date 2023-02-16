package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    @Autowired
    private final UserService userService;

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Добавление нового пользователя {}", user);

        return userService.addUser(user);
    }

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable Integer userId, @Valid @RequestBody User user) {
        log.info("Изменение пользователя {}", user);

        user.setId(userId);
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Получение списка пользователей.");

        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable Integer userId) {
        log.info("Получение пользователя с id {}", userId);

        return userService.getById(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        log.info("Удаление пользователя с id {}", userId);

        userService.deleteUser(userId);
    }
}
