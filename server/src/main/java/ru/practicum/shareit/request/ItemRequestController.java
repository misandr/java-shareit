package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Range;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static ru.practicum.shareit.Constants.HEADER_USER_ID;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Add new request for item {}, user {}", itemRequestDto, userId);

        return itemRequestService.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnItemRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Get requests by user id {}", userId);

        return itemRequestService.getOwnItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getOtherItemRequests(@RequestHeader(HEADER_USER_ID) Long userId,
                                                     Integer from, Integer size) {
        log.info("Get other requests item from {}, size = {}", from, size);
        return itemRequestService.getOtherItemRequests(userId, Range.of(from, size));
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@RequestHeader(HEADER_USER_ID) Long userId, @PathVariable Long requestId) {
        log.info("Get request by id {}", requestId);
        return itemRequestService.getItemRequestDto(userId, requestId);
    }
}
