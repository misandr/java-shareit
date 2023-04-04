package ru.practicum.shareit.exceptions;

public class ConflictException extends RuntimeException {
    public ConflictException(final String message) {
        super(message);
    }
}
