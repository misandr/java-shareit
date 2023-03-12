package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto item);

    List<ItemDto> getItems(Long userId);

    ItemDto getItemDto(Long userId, Long itemId);

    List<ItemDto> search(String query);

    CommentDto addComment(Long userId, Long itemId, Comment comment);
}
