package ru.practicum.shareit.exceptions;

public class ItemRequestNotFoundException extends NotFoundException {
    public ItemRequestNotFoundException(final long itemId) {
        super("Not found request " + itemId);
    }
}
