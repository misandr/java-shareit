package ru.practicum.shareit.request;

import ru.practicum.shareit.Range;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;


public interface ItemRequestService {

    ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getOwnItemRequests(Long userId);

    List<ItemRequestDto> getOtherItemRequests(Long userId, Range range);

    ItemRequestDto getItemRequestDto(Long userId, Long itemId);
}
