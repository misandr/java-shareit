package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByItem(Item item);

    List<Booking> findByBookerOrderByStartDesc(User booker);

    List<Booking> findByBookerAndStatusIs(User booker, Status status);

    List<Booking> findByItemAndBookerAndStatusEqualsAndStartIsBefore(Item item, User booker, Status status, LocalDateTime now);

    List<Booking> findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(User booker, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByBookerAndEndIsBeforeOrderByStartDesc(User booker, LocalDateTime now);

    List<Booking> findByBookerAndStartIsAfterOrderByStartDesc(User booker, LocalDateTime now);

    List<Booking> findByItemOwnerOrderByStartDesc(User owner);

    List<Booking> findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(User owner, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByItemOwnerAndEndIsBeforeOrderByStartDesc(User owner, LocalDateTime now);

    List<Booking> findByItemOwnerAndStartIsAfterOrderByStartDesc(User owner, LocalDateTime now);

    List<Booking> findByItemOwnerAndStatusIs(User owner, Status status);

    List<Booking> findByItemAndEndIsBeforeOrderByEndDesc(Item item, LocalDateTime end);

    List<Booking> findByItemAndStartIsAfterOrderByStartAsc(Item item, LocalDateTime start);
}
