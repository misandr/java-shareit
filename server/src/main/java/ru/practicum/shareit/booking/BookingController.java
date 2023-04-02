package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Range;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

import static ru.practicum.shareit.Constants.HEADER_USER_ID;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader(HEADER_USER_ID) Long userId,
                                 @RequestBody BookingDto bookingDto) {
        log.info("Add new booking {}, user {}", bookingDto, userId);
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto setApprove(@RequestHeader(HEADER_USER_ID) Long userId,
                                 @PathVariable Long bookingId,
                                 @RequestParam Boolean approved) {
        log.info("Change booking {}, approved {}, user {}", bookingId, approved, userId);
        return bookingService.setApprove(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(HEADER_USER_ID) Long userId,
                                 @PathVariable Long bookingId) {
        log.info("Get booking by id {}", bookingId);
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByState(@RequestHeader(HEADER_USER_ID) Long userId,
                                               @RequestParam String state,
                                               @RequestParam Integer from,
                                               @RequestParam Integer size) {
        log.info("Get booking by user id {}, state {}", userId, state);
        return bookingService.getBookings(userId, state, Range.of(from, size));
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookings(@RequestHeader(HEADER_USER_ID) Long userId,
                                        @RequestParam String state,
                                        @RequestParam Integer from,
                                        @RequestParam Integer size) {
        log.info("Get owner bookings, owner id {}", userId);
        return bookingService.getOwnerBookings(userId, state, Range.of(from, size));
    }
}
