package ru.practicum.shareit.exceptions;

import org.slf4j.Logger;

public class ExceptionFactory {
    public static RuntimeException createValidationException(Logger log, String message) {
        log.warn(message);
        return new ValidationException(message);
    }

    public static RuntimeException createItemNotFoundException(Logger log, Long itemId) {
        log.warn("Not found item " + itemId);
        return new ItemNotFoundException(itemId);
    }

    public static RuntimeException createForbiddenException(Logger log, String message) {
        log.warn(message);
        return new ForbiddenException(message);
    }

    public static RuntimeException createBookingNotFoundException(Logger log, Long bookingId) {
        log.warn("Not found booking " + bookingId);
        return new BookingNotFoundException(bookingId);
    }
}
