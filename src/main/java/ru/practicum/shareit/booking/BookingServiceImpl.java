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
import ru.practicum.shareit.exceptions.*;
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
        booking.setStatus(Status.WAITING);

        if (!item.isAvailable()) {
            throw ExceptionFactory.createValidationException(log,
                    String.format("Item %d not available!", bookingDto.getItemId()));
        }

        if (booking.getEnd().isBefore(DateUtils.now())) {
            throw ExceptionFactory.createValidationException(log,
                    String.format("End date of booking %d before now!",booking.getId()));
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            throw ExceptionFactory.createValidationException(log,
                    String.format("End date of booking %d before start!",booking.getId()));
        }

        if (booking.getStart().isBefore(DateUtils.now())) {
            throw ExceptionFactory.createValidationException(log,
                    String.format("Start date of booking %d before now!",booking.getId()));
        }

        if (booking.getStart().isEqual(booking.getEnd())) {
            throw ExceptionFactory.createValidationException(log,
                    String.format("Start for booking %d equal end!",booking.getId()));
        }

        if (item.getOwner().equals(user)) {
            throw ExceptionFactory.createItemNotFoundException(log, bookingDto.getItemId());
        }

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
                throw ExceptionFactory.createValidationException(log,
                        String.format("Item %d not available!", item.getId()));
            }
            User user = userService.getUser(userId);

            if (item.getOwner().equals(user)) {

                if (approved) {
                    if (booking.get().getStatus() == Status.APPROVED) {
                        throw ExceptionFactory.createValidationException(log,
                                String.format("Status booking %d is bad!", booking.get().getId()));
                    }

                    booking.get().setStatus(Status.APPROVED);
                } else {
                    booking.get().setStatus(Status.REJECTED);
                }

                Booking addedBooking = bookingRepository.save(booking.get());

                if (!addedBooking.equals(booking.get())) {
                    throw ExceptionFactory.createForbiddenException(log,"Can't approve booking " + booking.get().getId());
                }

                ItemDto itemDto = ItemMapper.toItemDto(item);

                return BookingMapper.toBookingDto(addedBooking, itemDto);
            } else {
                throw ExceptionFactory.createItemNotFoundException(log, item.getId());
            }
        } else {
            throw ExceptionFactory.createBookingNotFoundException(log, bookingId);
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
                throw ExceptionFactory.createBookingNotFoundException(log, bookingId);
            }
        } else {
            throw ExceptionFactory.createBookingNotFoundException(log, bookingId);
        }
    }

    @Override
    public List<BookingDto> getBookings(Long userId, String state, Integer from, Integer size) {
        LocalDateTime timeNow = DateUtils.now();

        User user = userService.getUser(userId);

        if ((from != null) && (size != null)) {
            if ((from < 0) || (size <= 0)) {
                throw ExceptionFactory.createValidationException(log,
                        String.format("Bad range for bookings for user %d!", userId));
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
                    throw ExceptionFactory.createValidationException(log, "Unknown state: UNSUPPORTED_STATUS");
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
                    throw ExceptionFactory.createValidationException(log, "Unknown state: UNSUPPORTED_STATUS");
            }

            return BookingMapper.toListBookingDto(bookings);
        } else {
            throw ExceptionFactory.createValidationException(log,
                    String.format("Bad range for bookings for user %d!", userId));
        }
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state, Integer from, Integer size) {

        LocalDateTime timeNow = DateUtils.now();

        User user = userService.getUser(userId);

        if ((from != null) && (size != null)) {
            if ((from < 0) || (size <= 0)) {
                throw ExceptionFactory.createValidationException(log,
                        String.format("Bad range for bookings for owner %d!", userId));
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
                    log.warn("Unknown state: UNSUPPORTED_STATUS");
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
                    log.warn("Unknown state: UNSUPPORTED_STATUS");
                    throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }
            return BookingMapper.toListBookingDto(bookings);
        } else {
            throw ExceptionFactory.createValidationException(log,
                    String.format("Bad range for bookings for owner %d!", userId));
        }
    }
}
