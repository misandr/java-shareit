package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
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
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;

    @Override
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

        if (booking.getEnd().isBefore(DateUtils.now())) {
            log.warn("End date of booking before now!");
            throw new ValidationException("End date of booking before now!");
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            log.warn("End date of booking before start!");
            throw new ValidationException("End date of booking before start!");
        }

        if (booking.getStart().isBefore(DateUtils.now())) {
            log.warn("Start date of booking before now!");
            throw new ValidationException("Start date of booking before now!");
        }

        if (booking.getStart().isEqual(booking.getEnd())) {
            log.warn("Start equal end!");
            throw new ValidationException("Start equal end!");
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

    @Override
    public BookingDto setApprove(Long userId, Long bookingId, Boolean approved) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {
            Item item = booking.get().getItem();

            if (!item.isAvailable()) {
                log.warn("Item not available!");
                throw new ValidationException("Item not available!");
            }
            User user = userService.getUser(userId);

            if (item.getOwner().equals(user)) {

                if (approved) {
                    if (booking.get().getStatus() == Status.APPROVED) {
                        log.warn("Status booking bad!");
                        throw new ValidationException("Status booking bad!");
                    }

                    booking.get().setStatus(Status.APPROVED);
                } else {
                    booking.get().setStatus(Status.REJECTED);
                }

                Booking addedBooking = bookingRepository.save(booking.get());

                ItemDto itemDto = ItemMapper.toItemDto(item);

                return BookingMapper.toBookingDto(addedBooking, itemDto);
            } else {
                log.warn("Not found item " + item.getId());
                throw new ItemNotFoundException(item.getId());
            }
        } else {
            log.warn("Not found booking " + bookingId);
            throw new BookingNotFoundException(bookingId);
        }
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {

            Item item = booking.get().getItem();
            User user = userService.getUser(userId);

            if (item.getOwner().equals(user) || booking.get().getBooker().equals(user)) {
                ItemDto itemDto = ItemMapper.toItemDto(item);
                return BookingMapper.toBookingDto(booking.get(), itemDto);
            } else {
                log.warn("Not found booking" + bookingId);
                throw new ItemNotFoundException(bookingId);
            }
        } else {
            log.warn("Not found booking " + bookingId);
            throw new ItemNotFoundException(bookingId);
        }
    }

    @Override
    public List<BookingDto> getBookings(Long userId, String state, Integer from, Integer size) {
        LocalDateTime timeNow = DateUtils.now();

        User user = userService.getUser(userId);

        if ((from != null) && (size != null)) {
            if ((from == -1) || (size == -1) || (size == 0)) {
                log.warn("Bad range for bookings!");
                throw new ValidationException("Bad range for bookings!");
            }

            Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");

            int newFrom = from / size;
            Pageable page = PageRequest.of(newFrom, size, sortByStart);

            Page<Booking> bookingsPage;
            switch (state) {
                case "ALL":
                    bookingsPage = bookingRepository.findByBooker(user, page);
                    break;
                case "CURRENT":
                    bookingsPage = bookingRepository.findByBookerAndStartIsBeforeAndEndIsAfter(user, timeNow, timeNow, page);
                    break;
                case "PAST":
                    bookingsPage = bookingRepository.findByBookerAndEndIsBefore(user, timeNow, page);
                    break;
                case "FUTURE":
                    bookingsPage = bookingRepository.findByBookerAndStartIsAfter(user, timeNow, page);
                    break;
                case "WAITING":
                    bookingsPage = bookingRepository.findByBookerAndStatusIs(user, Status.WAITING, page);
                    break;
                case "REJECTED":
                    bookingsPage = bookingRepository.findByBookerAndStatusIs(user, Status.REJECTED, page);
                    break;
                default:
                    log.warn("Status booking bad!");
                    throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }

            return BookingMapper.toListBookingDto(bookingsPage.getContent());
        } else if ((from == null) && (size == null)) {

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
        } else {
            log.warn("Bad range for bookings!");
            throw new ValidationException("Bad range for bookings!");
        }
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state, Integer from, Integer size) {

        LocalDateTime timeNow = DateUtils.now();

        User user = userService.getUser(userId);

        if ((from != null) && (size != null)) {
            if ((from == -1) || (size == -1) || (size == 0)) {
                log.warn("Bad range for bookings!");
                throw new ValidationException("Bad range for bookings!");
            }

            Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");

            int newFrom = from / size;
            Pageable page = PageRequest.of(newFrom, size, sortByStart);

            Page<Booking> bookingsPage;
            switch (state) {
                case "ALL":
                    bookingsPage = bookingRepository.findByItemOwner(user, page);
                    break;
                case "CURRENT":
                    bookingsPage = bookingRepository.findByItemOwnerAndStartIsBeforeAndEndIsAfter(user, timeNow, timeNow, page);
                    break;
                case "PAST":
                    bookingsPage = bookingRepository.findByItemOwnerAndEndIsBefore(user, timeNow, page);
                    break;
                case "FUTURE":
                    bookingsPage = bookingRepository.findByItemOwnerAndStartIsAfter(user, timeNow, page);
                    break;
                case "WAITING":
                    bookingsPage = bookingRepository.findByItemOwnerAndStatusIs(user, Status.WAITING, page);
                    break;
                case "REJECTED":
                    bookingsPage = bookingRepository.findByItemOwnerAndStatusIs(user, Status.REJECTED, page);
                    break;
                default:
                    log.warn("Status booking bad!");
                    throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }

            return BookingMapper.toListBookingDto(bookingsPage.getContent());
        } else if ((from == null) && (size == null)) {
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
        } else {
            log.warn("Bad range for bookings!");
            throw new ValidationException("Bad range for bookings!");
        }
    }
}
