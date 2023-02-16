package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ForbiddenException;
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
            log.warn("Запрос пустой!");
            throw new ValidationException("Запрос пустой!");
        }

        if ((item.getName() == null) || item.getName().isBlank()) {
            log.warn("Имя неправильное!");
            throw new ValidationException("Имя неправлиьное!");
        }

        item.setId(generateId);
        items.put(generateId, item);

        generateId++;
        System.out.println(item);
        return item;
    }

    public Item updateItem(Item item) {
        Item findedItem = getItem(item.getId());
        if (findedItem != null) {
            if (findedItem.getOwner() != item.getOwner()) {
                throw new ForbiddenException("Другой пользователь!");
            }

            if (item.getName() != null) {
                findedItem.setName(item.getName());
            }

            if (item.getDescription() != null) {
                findedItem.setDescription(item.getDescription());
            }

            if (item.getAvailable() != null) {
                findedItem.setAvailable(item.getAvailable());
            }
        } else {
            throw new NotFoundException("Нет такого вещи!");
        }
        return findedItem;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item getItem(Integer id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            log.warn("Нет такого пользователя!");
            throw new NotFoundException("Нет такого пользователя!");
        }
    }
}
