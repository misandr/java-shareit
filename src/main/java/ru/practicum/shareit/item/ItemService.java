package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto item);

    List<ItemDto> getItems(Long userId, Integer from, Integer size);

    ItemDto getItemDto(Long userId, Long itemId);

    List<ItemDto> search(String query, Integer from, Integer size);

    CommentDto addComment(Long userId, Long itemId, CommentDto comment);
}
