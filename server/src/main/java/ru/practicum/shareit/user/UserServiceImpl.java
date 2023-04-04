package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public User addUser(User user) {
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
