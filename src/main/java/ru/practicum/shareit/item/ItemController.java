package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.Constants.HEADER_USER_ID;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestHeader(HEADER_USER_ID) Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Add new item {}, user {}", itemDto, userId);

        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(HEADER_USER_ID) Long userId,
                              @PathVariable Long itemId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Change item {}, user {}", itemDto, userId);

        itemDto.setId(itemId);
        return itemService.updateItem(userId, itemDto);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Get items by user id {}", userId);

        return itemService.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(HEADER_USER_ID) Long userId, @PathVariable Long itemId) {
        log.info("Get by id {}", itemId);

        return itemService.getItemDto(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Search item by {}", text);
        String query = text.toLowerCase();
        return itemService.search(query);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HEADER_USER_ID) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody Comment comment) {
        log.info("Search item by {}", comment);
        return itemService.addComment(userId, itemId, comment);
    }
}
