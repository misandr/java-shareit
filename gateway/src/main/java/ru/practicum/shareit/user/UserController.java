package ru.practicum.shareit.user;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.*;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody @Valid User user) {
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
        ResponseEntity<Object> r = userClient.addUser(user);

        // Так?
        Gson gson = new Gson();
        if (r.getStatusCode() == HttpStatus.OK) {

            User gotUser = gson.fromJson(r.getBody().toString(), User.class);
            log.info("Add new user {}", user);
            return new ResponseEntity<User>(gotUser, r.getHeaders(), r.getStatusCode());
        } else {
            String responseString = new String((byte[]) r.getBody(), StandardCharsets.UTF_8);
            ErrorResponse errorResponse = gson.fromJson(responseString, ErrorResponse.class);
            switch (r.getStatusCode().value()) {
                case 400:
                    throw new ValidationException(errorResponse.getError());
                case 403:
                    throw new ForbiddenException(errorResponse.getError());
                case 404:
                    throw new NotFoundException(errorResponse.getError());
                case 409:
                    throw new ConflictException(errorResponse.getError());
                default:
                    throw new RuntimeException(errorResponse.getError());
            }
        }
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
