package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.Range;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final UserServiceImpl userService;

    private final CommentRepository commentRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {

        User user = userService.getUser(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);

        Item addedItem = itemRepository.save(item);
        item.setId(addedItem.getId());

        if (!addedItem.equals(item)) {
            log.warn("Can't add item " + item.getId());
            throw new ForbiddenException("Can't add item " + item.getId());
        }

        return ItemMapper.toItemDto(addedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto) {
        Item gettedItem = getItem(itemDto.getId());
        User user = userService.getUser(userId);

        if (!gettedItem.getOwner().equals(user)) {
            log.warn("Another user for item " + itemDto.getId());
            throw new ForbiddenException("Another user for item " + itemDto.getId());
        }

        if (itemDto.getName() != null) {
            gettedItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            gettedItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            gettedItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(gettedItem);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItems(Long userId, Range range) {
        User user = userService.getUser(userId);

        List<ItemDto> listItemDto = new ArrayList<>();

        int newFrom = range.getFrom() / range.getSize();
        Pageable page = PageRequest.of(newFrom, range.getSize());

        Page<Item> itemsPage = itemRepository.findByOwnerOrderByIdAsc(user, page);

        List<Item> items = itemsPage.getContent();


        for (Item item : items) {
            ItemDto itemDto = ItemMapper.toItemDto(item);

            List<Comment> comments = commentRepository.findByItem(item);

            itemDto.setComments(CommentMapper.toCommentsDto(comments));

            if (item.getOwner().equals(user)) {
                itemDto.setLastBooking(findLastBooking(item));
                itemDto.setNextBooking(findNextBooking(item));
            }

            listItemDto.add(itemDto);
        }
        log.error("For user {} items {}", userId, listItemDto);
        return listItemDto;
    }

    @Override
    public ItemDto getItemDto(Long userId, Long itemId) {
        User user = userService.getUser(userId);
        Item item = getItem(itemId);

        ItemDto itemDto = ItemMapper.toItemDto(item);

        List<Comment> comments = commentRepository.findByItem(item);

        itemDto.setComments(CommentMapper.toCommentsDto(comments));

        if (item.getOwner().equals(user)) {
            itemDto.setLastBooking(findLastBooking(item));
            itemDto.setNextBooking(findNextBooking(item));
        }

        return itemDto;
    }

    @Override
    public Item getItem(Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            return item.get();
        } else {
            log.warn("Not found item " + itemId);
            throw new ItemNotFoundException(itemId);
        }
    }

    @Override
    public List<ItemDto> search(String query, Range range) {
        List<ItemDto> listItemDto = new ArrayList<>();

        if (!query.isBlank()) {
            int newFrom = range.getFrom() / range.getSize();
            Pageable page = PageRequest.of(newFrom, range.getSize());

            Page<Item> itemsPage = itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(query, query, true, page);
            for (Item item : itemsPage.getContent()) {
                listItemDto.add(ItemMapper.toItemDto(item));
            }
        }

        return listItemDto;
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userService.getUser(userId);
        Item item = getItem(itemId);
        List<Booking> bookings = bookingRepository.findByItemAndBookerAndStatusEqualsAndStartIsBefore(item, user, Status.APPROVED, DateUtils.now());

        if (bookings.size() > 0) {

            Comment comment = new Comment();

            comment.setText(commentDto.getText());
            comment.setCreated(DateUtils.now());
            comment.setItem(item);
            comment.setAuthor(user);

            Comment savedComment = commentRepository.save(comment);
            if (!savedComment.equals(comment)) {
                log.warn("Can't add comment " + comment.getId());
                throw new ForbiddenException("Can't add comment " + comment.getId());
            }

            CommentDto savedCommentDto = CommentMapper.toCommentDto(savedComment);

            savedCommentDto.setAuthorName(user.getName());
            return savedCommentDto;
        } else {
            log.warn("No bookings for user " + user.getId() + ", item " + item.getId());
            throw new ValidationException("No bookings for user " + user.getId() + ", item " + item.getId());
        }
    }

    private BookingShortInfoDto findLastBooking(Item item) {

        List<Booking> bookings = bookingRepository.findByItemAndStartIsBeforeOrderByEndDesc(item, DateUtils.now());

        if (bookings.size() > 0) {
            Booking booking = bookings.get(0);

            if (booking.getStatus() == Status.APPROVED) {
                BookingShortInfoDto lastBooking = new BookingShortInfoDto();
                lastBooking.setId(booking.getId());
                lastBooking.setBookerId(booking.getBooker().getId());

                return lastBooking;
            }
        }
        return null;
    }

    private BookingShortInfoDto findNextBooking(Item item) {
        List<Booking> bookings = bookingRepository.findByItemAndStartIsAfterOrderByStartAsc(item, DateUtils.now());

        if (bookings.size() > 0) {
            Booking booking = bookings.get(0);

            if (booking.getStatus() == Status.APPROVED) {
                BookingShortInfoDto nextBooking = new BookingShortInfoDto();
                nextBooking.setId(booking.getId());
                nextBooking.setBookerId(booking.getBooker().getId());

                return nextBooking;
            }
        }
        return null;
    }
}
