package ru.practicum.shareit.exceptions;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(final int userId) {
        super("Not found user " + userId);
    }
}
