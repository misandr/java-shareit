package ru.practicum.shareit.item;

import ru.practicum.shareit.Range;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto item);

    List<ItemDto> getItems(Long userId, Range range);

    ItemDto getItemDto(Long userId, Long itemId);

    Item getItem(Long itemId);

    List<ItemDto> search(String query, Range range);

    CommentDto addComment(Long userId, Long itemId, CommentDto comment);
}
