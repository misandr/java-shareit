package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.Status;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class BookingTest {

    @Test
    void testBooking() {

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        Booking booking = new Booking(1L, start, end, null, null, Status.WAITING);

        assertNotNull(booking.getId());
        assertEquals(booking.getStart(),start);
        assertEquals(booking.getEnd(),end);
    }
}