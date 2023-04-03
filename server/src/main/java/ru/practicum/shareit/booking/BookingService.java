package ru.practicum.shareit.booking;

import ru.practicum.shareit.Range;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto addBooking(Long userId, BookingDto booking);

    BookingDto setApprove(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingDto(Long userId, Long itemId);

    List<BookingDto> getBookings(Long userId, String state, Range range);

    List<BookingDto> getOwnerBookings(Long userId, String state, Range range);
}
