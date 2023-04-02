package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.Constants.HEADER_USER_ID;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        if (itemDto.getName() == null) {
            log.warn("Name is null!");
            throw new NullValidationException("Name");
        }

        if (itemDto.getName().isBlank()) {
            log.warn("Name is empty!");
            throw new ValidationException("Name is empty!");
        }

        if (itemDto.getAvailable() == null) {
            log.warn("Available is null!");
            throw new NullValidationException("Available");
        }

        if (itemDto.getDescription() == null) {
            log.warn("Description is null!");
            throw new NullValidationException("Description");
        }

        log.info("Add new item {}, user {}", itemDto, userId);
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(HEADER_USER_ID) long userId,
                              @PathVariable Long itemId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Change item {}, user {}", itemDto, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(HEADER_USER_ID) long userId,
                                           @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                           @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get items by user id {}", userId);
        return itemClient.getItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(HEADER_USER_ID) long userId,
                                          @PathVariable Long itemId) {
        log.info("Get by id {}", itemId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Search item by {}", text);
        String query = text.toLowerCase();
        return itemClient.search(query, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(HEADER_USER_ID) long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        if (commentDto.getText().isBlank()) {
            log.warn("Text of comment is empty!");
            throw new ValidationException("Text of comment is empty!");
        }

        log.info("Search item by {}", commentDto);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
