package ru.practicum.shareit.exceptions;

public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(final long itemId) {
        super("Not found booking " + itemId);
    }
}
