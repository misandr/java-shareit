package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})

public class BookingRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findByBookerOrderByStartDescTest() {
        User user = userRepository.save(new User(1L, "Иван","q@q.net"));
        User ownUser = userRepository.save(new User(0L, "Пётр", "j@j.ru"));

        Item item = itemRepository.save(
                new Item(0L, "Item 1", "Good", true, ownUser, 0L));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        Booking booking = bookingRepository.save(new Booking(0L, start, end, item, user, Status.WAITING));

        List<Booking> sourceBookings = List.of(booking);

        List<Booking> bookings = bookingRepository.findByBookerOrderByStartDesc(user);

        assertThat(bookings, hasSize(bookings.size()));
        for (Booking sourceBooking : sourceBookings) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker()))
            )));
        }
    }
}
