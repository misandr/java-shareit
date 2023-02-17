package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {
    private int generateId;
    private final Map<Integer, Item> items;

    public InMemoryItemStorage() {
        generateId = 1;
        items = new HashMap<>();
    }

    public Item addItem(Item item) {

        if (item == null) {
            log.warn("Request for add item is empty!");
            throw new ValidationException("Request for add item is empty!");
        }

        if ((item.getName() == null) || item.getName().isBlank()) {
            log.warn("Bad name for item!");
            throw new ValidationException("Bad name for item!");
        }

        item.setId(generateId);
        items.put(generateId, item);

        generateId++;
        System.out.println(item);
        return item;
    }

    public Item updateItem(Item item) {
        Item gettedItem = getItem(item.getId());

        if (gettedItem.getOwner() != item.getOwner()) {
            log.warn("Another user!");
            throw new ForbiddenException("Another user!");
        }

        if (item.getName() != null) {
            gettedItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            gettedItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            gettedItem.setAvailable(item.getAvailable());
        }

        return gettedItem;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item getItem(Integer id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            log.warn("Not found item with id " + id);
            throw new ItemNotFoundException(id);
        }
    }
}
