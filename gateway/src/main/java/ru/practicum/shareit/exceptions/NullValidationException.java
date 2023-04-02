package ru.practicum.shareit.exceptions;

public class NullValidationException extends ValidationException {
    public NullValidationException(final String field) {
        super(field + " is null!");
    }
}
