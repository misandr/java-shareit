package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

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

        try {
            return repository.save(user);
        } catch (RuntimeException e) {
            throw new ConflictException("User didn't save!");
        }
    }

    public User updateUser(User user) {
        if (repository.existsById(user.getId())) {
            User gettedUser = repository.getReferenceById(user.getId());

            if (user.getEmail() != null) {
                gettedUser.setEmail(user.getEmail());
            }

            if (user.getName() != null) {
                gettedUser.setName(user.getName());
            }

            try {
                return repository.save(gettedUser);
            } catch (RuntimeException e) {
                throw new ConflictException("User didn't save!");
            }
        } else {
            log.warn("Not found user " + user.getId());
            throw new UserNotFoundException(user.getId());
        }
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public User getUser(Long userId) {
        Optional<User> user = repository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            log.warn("Not found user " + userId);
            throw new UserNotFoundException(userId);
        }
    }

    public void deleteUser(Long userId) {
        if (repository.existsById(userId)) {
            repository.deleteById(userId);
        } else {
            log.warn("Not found user " + userId);
            throw new UserNotFoundException(userId);
        }
    }
}
