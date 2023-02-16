package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Integer userId, ItemDto itemDto);

    ItemDto updateItem(Integer userId, ItemDto item);

    List<ItemDto> getItems(Integer userId);

    ItemDto getItem(Integer itemId);

    List<ItemDto> search(String query);
}
