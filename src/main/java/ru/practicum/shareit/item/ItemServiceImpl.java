package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.DateUtils;
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
            throw ExceptionFactory.createForbiddenException(log,
                    String.format("Another user for item %d!", itemDto.getId()));
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
    public List<ItemDto> getItems(Long userId, Integer from, Integer size) {
        User user = userService.getUser(userId);

        List<ItemDto> listItemDto = new ArrayList<>();
        List<Item> items;

        if ((from != null) && (size != null)) {
            if ((from < 0) || (size <= 0)) {
                throw ExceptionFactory.createValidationException(log,
                        String.format("Bad range(form %d, size %d) for get items user %d!", from, size, userId));
            }
            int newFrom = from / size;
            Pageable page = PageRequest.of(newFrom, size);

            Page<Item> itemssPage = itemRepository.findByOwner(user, page);

            items = itemssPage.getContent();
        } else if ((from == null) && (size == null)) {
            items = itemRepository.findByOwner(user);
        } else {
            throw ExceptionFactory.createValidationException(log,
                    String.format("Bad range(form or size is null) for get items user %d!", userId));
        }

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
    public List<ItemDto> search(String query, Integer from, Integer size) {
        List<ItemDto> listItemDto = new ArrayList<>();

        if ((from != null) && (size != null)) {
            if ((from < 0) || (size <= 0)) {
                throw ExceptionFactory.createValidationException(log,
                        String.format("Bad range(form %d, size %d) for search!", from, size));
            }

            if (!query.isBlank()) {
                int newFrom = from / size;
                Pageable page = PageRequest.of(newFrom, size);

                Page<Item> itemsPage = itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(query, query, true, page);
                for (Item item : itemsPage.getContent()) {
                    listItemDto.add(ItemMapper.toItemDto(item));
                }
            }
        } else if ((from == null) && (size == null)) {
            if (!query.isBlank()) {
                List<Item> items = itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(query, query, true);
                for (Item item : items) {
                    listItemDto.add(ItemMapper.toItemDto(item));
                }
            }
        } else {
            throw ExceptionFactory.createValidationException(log, "Bad range(form or size is null) for search!");
        }

        return listItemDto;
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userService.getUser(userId);
        Item item = getItem(itemId);
        List<Booking> bookings = bookingRepository.findByItemAndBookerAndStatusEqualsAndStartIsBefore(item, user, Status.APPROVED, DateUtils.now());

        if (bookings.size() > 0) {
            if (commentDto.getText().isBlank()) {
                log.warn("Text of comment is empty!");
                throw new ValidationException("Text of comment is empty!");
            }
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
            throw ExceptionFactory.createValidationException(log,
                    String.format("No bookings for user %d, item %d!", user.getId(), item.getId()));
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
