package ru.practicum.shareit.exceptions;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(final long itemId) {
        super("Not found item " + itemId);
    }
}
