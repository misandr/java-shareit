package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.Constants.HEADER_USER_ID;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {

    @Autowired
    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestHeader(HEADER_USER_ID) Integer userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Add new item {}, user {}", itemDto, userId);

        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(HEADER_USER_ID) Integer userId,
                              @PathVariable Integer itemId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Change item {}, user {}", itemDto, userId);

        itemDto.setId(itemId);
        return itemService.updateItem(userId, itemDto);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(HEADER_USER_ID) Integer userId) {
        log.info("Get items by user id {}", userId);

        return itemService.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Integer itemId) {
        log.info("Get by id {}", itemId);

        return itemService.getItem(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Search item by {}", text);
        String query = text.toLowerCase();
        return itemService.search(query);
    }
}
