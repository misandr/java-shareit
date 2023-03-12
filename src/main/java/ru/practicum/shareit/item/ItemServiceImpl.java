package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        return ItemMapper.toItemDto(addedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto) {
        Item gettedItem = getItem(itemDto.getId());
        User user = userService.getUser(userId);

        if (!gettedItem.getOwner().equals(user)) {
            log.warn("Another user!");
            throw new ForbiddenException("Another user!");
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
    public List<ItemDto> getItems(Long userId) {
        User user = userService.getUser(userId);

        List<ItemDto> listItemDto = new ArrayList<>();

        List<Item> items = itemRepository.findByOwnerId(userId);
        for (Item item : items) {
            ItemDto itemDto = ItemMapper.toItemDto(item);

            List<Comment> comments = commentRepository.findByItem(item);
            List<CommentDto> commentsDto = new ArrayList<>();

            for (Comment comment : comments) {
                CommentDto commentDto = CommentMapper.toCommentDto(comment);
                commentsDto.add(commentDto);
            }

            itemDto.setComments(commentsDto);

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
        List<CommentDto> commentsDto = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDto commentDto = CommentMapper.toCommentDto(comment);

            commentsDto.add(commentDto);
        }
        itemDto.setComments(commentsDto);

        if (item.getOwner().equals(user)) {
            itemDto.setLastBooking(findLastBooking(item));
            itemDto.setNextBooking(findNextBooking(item));
        }

        return itemDto;
    }

    public Item getItem(Long itemId) {
        if (itemRepository.existsById(itemId)) {
            return itemRepository.getReferenceById(itemId);
        } else {
            log.warn("Not found item " + itemId);
            throw new ItemNotFoundException(itemId);
        }
    }

    @Override
    public List<ItemDto> search(String query) {
        List<ItemDto> listItemDto = new ArrayList<>();

        if (!query.isBlank()) {
            for (Item item : itemRepository.findAll()) {

                if (item.isAvailable()) {
                    if (item.getName().toLowerCase().contains(query)) {
                        listItemDto.add(ItemMapper.toItemDto(item));
                        continue;
                    }

                    if (item.getDescription().toLowerCase().contains(query)) {
                        listItemDto.add(ItemMapper.toItemDto(item));
                    }
                }
            }
        }
        return listItemDto;
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, Comment comment) {
        User user = userService.getUser(userId);
        Item item = getItem(itemId);
        List<Booking> bookings = bookingRepository.findByItemAndBookerAndStatusEqualsAndStartIsBefore(item, user, Status.APPROVED, DateUtils.now());

        if (bookings.size() > 0) {
            if (comment.getText().isBlank()) {
                log.warn("Text of comment is empty!");
                throw new ValidationException("Text of comment is empty!");
            }

            comment.setCreated(DateUtils.now());
            comment.setItem(item);
            comment.setAuthor(user);
            Comment savedComment = commentRepository.save(comment);
            CommentDto commentDto = CommentMapper.toCommentDto(savedComment);

            commentDto.setAuthorName(user.getName());
            return commentDto;
        } else {
            log.warn("No bookings!");
            throw new ValidationException("No bookings!");
        }
    }

    private BookingShortInfoDto findLastBooking(Item item) {
        List<Booking> bookings = bookingRepository.findByItemAndEndIsBeforeOrderByEndDesc(item, DateUtils.now());

        if (bookings.size() > 0) {
            Booking booking = bookings.get(0);
            System.out.println(booking);
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
            System.out.println(booking);

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
