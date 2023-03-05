package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserServiceImpl;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("ItemServiceImpl")
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;

    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        User user = userService.getUser(userId);
        Item item = itemService.getItem(bookingDto.getItemId());

        Booking booking = BookingMapper.toBooking(bookingDto);

        booking.setBooker(user);
        booking.setItem(item);

        if (!item.isAvailable()) {
            log.warn("Item not available!");
            throw new ValidationException("Item not available!");
        }

        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            log.warn("End date of booking before now!");
            throw new ValidationException("End date of booking before now!");
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            log.warn("End date of booking before start!");
            throw new ValidationException("End date of booking before start!");
        }

        if (booking.getStart().isBefore(LocalDateTime.now())) {
            log.warn("Start date of booking before now!");
            throw new ValidationException("Start date of booking before now!");
        }

        if (item.getOwner().equals(user)) {
            log.warn("Own item!");
            throw new ItemNotFoundException(item.getId());
        }

        booking.setStatus(Status.WAITING);
        booking.setBooker(user);
        Booking addedBooking = bookingRepository.save(booking);

        ItemDto itemDto = ItemMapper.toItemDto(item);
        return BookingMapper.toBookingDto(addedBooking, itemDto);
    }

    public BookingDto setApprove(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.getReferenceById(bookingId);

        Item item = booking.getItem();

        if (!item.isAvailable()) {
            log.warn("Item not available!");
            throw new ValidationException("Item not available!");
        }
        User user = userService.getUser(userId);

        if (item.getOwner().equals(user)) {

            if (approved) {
                if (booking.getStatus() == Status.APPROVED) {
                    log.warn("Status booking bad!");
                    throw new ValidationException("Status booking bad!");
                }

                booking.setStatus(Status.APPROVED);
            } else {
                booking.setStatus(Status.REJECTED);
            }

            Booking addedBooking = bookingRepository.save(booking);

            ItemDto itemDto = ItemMapper.toItemDto(item);

            return BookingMapper.toBookingDto(addedBooking, itemDto);
        } else {
            log.warn("Not found booking " + bookingId);
            throw new ItemNotFoundException(bookingId);
        }
    }

    public BookingDto getBooking(Long userId, Long bookingId) {
        if (bookingRepository.existsById(bookingId)) {

            Booking booking = bookingRepository.getReferenceById(bookingId);
            Item item = booking.getItem();
            User user = userService.getUser(userId);

            if (item.getOwner().equals(user) || booking.getBooker().equals(user)) {
                ItemDto itemDto = ItemMapper.toItemDto(item);
                return BookingMapper.toBookingDto(booking, itemDto);
            } else {
                log.warn("Not found booking" + bookingId);
                throw new ItemNotFoundException(bookingId);
            }
        } else {
            log.warn("Not found booking " + bookingId);
            throw new ItemNotFoundException(bookingId);
        }
    }

    public List<BookingDto> getBookings(Long userId, String state) {
        LocalDateTime timeNow = LocalDateTime.now();

        User user = userService.getUser(userId);

        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerOrderByStartDesc(user);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(user, timeNow, timeNow);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerAndEndIsBeforeOrderByStartDesc(user, timeNow);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerAndStartIsAfterOrderByStartDesc(user, timeNow);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerAndStatusIs(user, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerAndStatusIs(user, Status.REJECTED);
                break;
            default:
                log.warn("Status booking bad!");
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }

        return BookingMapper.toListBookingDto(bookings);
    }

    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        LocalDateTime timeNow = LocalDateTime.now();

        User user = userService.getUser(userId);
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerOrderByStartDesc(user);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(user, timeNow, timeNow);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerAndEndIsBeforeOrderByStartDesc(user, timeNow);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerAndStartIsAfterOrderByStartDesc(user, timeNow);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerAndStatusIs(user, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerAndStatusIs(user, Status.REJECTED);
                break;
            default:
                log.warn("Status booking bad!");
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }

        return BookingMapper.toListBookingDto(bookings);
    }
}
