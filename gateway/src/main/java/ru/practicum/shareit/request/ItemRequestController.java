package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.Constants.HEADER_USER_ID;

@Slf4j
@RestController
@RequestMapping("/requests")
@AllArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @Valid @RequestBody ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null) {
            log.warn("Description for new request is null!");
            throw new NullValidationException("Description");
        }

        log.info("Add new request for item {}, user {}", itemRequestDto, userId);
        return itemRequestClient.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnItemRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Get requests by user id {}", userId);
        return itemRequestClient.getOwnItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getOtherItemRequests(@RequestHeader(HEADER_USER_ID) Long userId,
                                                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                       @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get other requests item from {}, size = {}", from, size);
        return itemRequestClient.getOtherItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(HEADER_USER_ID) Long userId, @PathVariable Long requestId) {
        log.info("Get request by id {}", requestId);
        return itemRequestClient.getItemRequest(userId, requestId);
    }
}
