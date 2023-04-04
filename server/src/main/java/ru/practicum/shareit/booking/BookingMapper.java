package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {
    public static Booking toBooking(BookingDto bookingDto) {

        Booking booking = new Booking();

        booking.setId(bookingDto.getId());

        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());

        booking.setStatus(bookingDto.getStatus());

        return booking;
    }

    public static BookingDto toBookingDto(Booking booking, ItemDto itemDto) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId(),
                itemDto,
                booking.getBooker(),
                booking.getStatus()
        );
    }

    public static List<BookingDto> toListBookingDto(List<Booking> bookings) {
        List<BookingDto> bookingsDto = new ArrayList<>();

        for (Booking booking : bookings) {
            Item item = booking.getItem();
            ItemDto itemDto = ItemMapper.toItemDto(item);
            bookingsDto.add(BookingMapper.toBookingDto(booking, itemDto));
        }

        return bookingsDto;
    }
}
