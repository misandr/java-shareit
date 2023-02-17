package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import ru.practicum.shareit.exceptions.ValidationException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("ItemServiceImpl")
public class ItemServiceImpl implements ItemService {
    @Autowired
    private final ItemStorage itemStorage;

    @Autowired
    private final UserService userService;

    @Override
    public ItemDto addItem(Integer userId, ItemDto itemDto) {
        User user = userService.getUser(userId);

        if (user == null) {
            throw new UserNotFoundException(userId);
        }

        if (itemDto.getAvailable() == null) {
            log.warn("Available is null!");
            throw new NullValidationException("Available");
        }

        if (itemDto.getDescription() == null) {
            log.warn("Description is null!");
            throw new NullValidationException("Description");
        }
//        if(userService)
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        Item addedItem = itemStorage.addItem(item);

        return ItemMapper.toItemDto(addedItem);
    }

    @Override
    public ItemDto updateItem(Integer userId, ItemDto itemDto) {
        User user = userService.getUser(userId);

        if (user == null) {
            throw new UserNotFoundException(userId);
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        Item updatedItem = itemStorage.updateItem(item);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItems(Integer userId) {
        List<ItemDto> listItemDto = new ArrayList<>();
        for (Item item : itemStorage.getItems()) {
            if (item.getOwner().getId() == userId) {
                listItemDto.add(ItemMapper.toItemDto(item));
            }
        }
        return listItemDto;
    }

    @Override
    public ItemDto getItem(Integer itemId) {
        return ItemMapper.toItemDto(itemStorage.getItem(itemId));
    }

    @Override
    public List<ItemDto> search(String query) {
        List<ItemDto> listItemDto = new ArrayList<>();

        if (!query.isBlank()) {
            for (Item item : itemStorage.getItems()) {

                if (item.isAvailable()) {
                    if (item.getName().toLowerCase().contains(query)) {
                        listItemDto.add(ItemMapper.toItemDto(item));
                        continue;
                    }

                    if (item.getDescription().toLowerCase().contains(query)) {
                        listItemDto.add(ItemMapper.toItemDto(item));
                    }
                }
            }
        }
        return listItemDto;
    }
}
