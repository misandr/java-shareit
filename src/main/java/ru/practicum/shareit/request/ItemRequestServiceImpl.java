package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.NullValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;

    private final ItemRepository itemRepository;

    private final UserServiceImpl userService;

    @Override
    public ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null) {
            log.warn("Description is null!");
            throw new NullValidationException("Description");
        }

        User user = userService.getUser(userId);

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(DateUtils.now());
        ItemRequest addedItemRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(addedItemRequest);
    }

    @Override
    public List<ItemRequestDto> getOwnItemRequests(Long userId) {

        User user = userService.getUser(userId);

        List<ItemRequestDto> listItemRequests = new ArrayList<>();
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestor(user);
        for (ItemRequest itemRequest : itemRequests) {

            ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

            List<Item> items = itemRepository.findByRequestId(itemRequest.getId());

            List<ItemDto> itemsDto = new ArrayList<>();
            for (Item item : items) {
                itemsDto.add(ItemMapper.toItemDto(item));
            }
            itemRequestDto.setItems(itemsDto);

            listItemRequests.add(itemRequestDto);
        }

        return listItemRequests;
    }

    @Override
    public List<ItemRequestDto> getOtherItemRequests(Long userId, Integer from, Integer size) {
        User user = userService.getUser(userId);
        List<ItemRequestDto> listItemRequests = new ArrayList<>();

        if ((from != null) && (size != null)) {
            Pageable page = PageRequest.of(from, size);
            do {

                Page<ItemRequest> itemRequestPage = itemRequestRepository.findAll(page);

                itemRequestPage.getContent().forEach(itemRequest -> {

                    if (!itemRequest.getRequestor().equals(user)) {
                        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

                        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());

                        List<ItemDto> itemsDto = new ArrayList<>();
                        for (Item item : items) {
                            itemsDto.add(ItemMapper.toItemDto(item));
                        }
                        itemRequestDto.setItems(itemsDto);

                        listItemRequests.add(itemRequestDto);
                    }
                });

                if (itemRequestPage.hasNext()) {
                    page = PageRequest.of(itemRequestPage.getNumber() + 1, itemRequestPage.getSize(), itemRequestPage.getSort()); // или для простоты -- userPage.nextOrLastPageable()
                } else {
                    page = null;
                }
            } while (page != null);
        }
        return listItemRequests;
    }

    @Override
    public ItemRequestDto getItemRequestDto(Long userId, Long requestId) {
        User user = userService.getUser(userId);

        if (itemRequestRepository.existsById(requestId)) {
            ItemRequest itemRequest = itemRequestRepository.getReferenceById(requestId);

            ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

            List<Item> items = itemRepository.findByRequestId(itemRequest.getId());

            List<ItemDto> itemsDto = new ArrayList<>();
            for (Item item : items) {
                itemsDto.add(ItemMapper.toItemDto(item));
            }

            itemRequestDto.setItems(itemsDto);

            return itemRequestDto;
        } else {
            log.warn("Not found request " + requestId);
            throw new ItemRequestNotFoundException(requestId);
        }
    }
}