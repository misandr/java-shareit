package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends PagingAndSortingRepository<Booking, Long> {

    List<Booking> findByBookerOrderByStartDesc(User booker);

    Page<Booking> findByBooker(User booker, Pageable pageable);

    List<Booking> findByBookerAndStatusIs(User booker, Status status);

    Page<Booking> findByBookerAndStatusIs(User booker, Status status, Pageable pageable);

    List<Booking> findByItemAndBookerAndStatusEqualsAndStartIsBefore(Item item, User booker, Status status, LocalDateTime now);

    List<Booking> findByBookerAndStartIsBeforeAndEndIsAfterOrderByEndDesc(User booker, LocalDateTime now1, LocalDateTime now2);

    Page<Booking> findByBookerAndStartIsBeforeAndEndIsAfter(User booker, LocalDateTime now1, LocalDateTime now2, Pageable pageable);

    List<Booking> findByBookerAndEndIsBeforeOrderByEndDesc(User booker, LocalDateTime now);

    Page<Booking> findByBookerAndEndIsBefore(User booker, LocalDateTime now, Pageable pageable);

    List<Booking> findByBookerAndStartIsAfterOrderByEndDesc(User booker, LocalDateTime now);

    Page<Booking> findByBookerAndStartIsAfter(User booker, LocalDateTime now, Pageable pageable);

    List<Booking> findByItemOwnerOrderByStartDesc(User owner);

    Page<Booking> findByItemOwner(User owner, Pageable pageable);

    List<Booking> findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByEndDesc(User owner, LocalDateTime now1, LocalDateTime now2);

    Page<Booking> findByItemOwnerAndStartIsBeforeAndEndIsAfter(User owner, LocalDateTime now1, LocalDateTime now2, Pageable pageable);

    List<Booking> findByItemOwnerAndEndIsBeforeOrderByEndDesc(User owner, LocalDateTime now);

    Page<Booking> findByItemOwnerAndEndIsBefore(User owner, LocalDateTime now, Pageable pageable);

    List<Booking> findByItemOwnerAndStartIsAfterOrderByEndDesc(User owner, LocalDateTime now);

    Page<Booking> findByItemOwnerAndStartIsAfter(User owner, LocalDateTime now, Pageable pageable);

    List<Booking> findByItemOwnerAndStatusIs(User owner, Status status);

    Page<Booking> findByItemOwnerAndStatusIs(User owner, Status status, Pageable pageable);

    List<Booking> findByItemAndStartIsBeforeOrderByEndDesc(Item item, LocalDateTime end);

    List<Booking> findByItemAndStartIsAfterOrderByStartAsc(Item item, LocalDateTime start);

    List<Booking> findByItemOrderByEndDesc(Item item);
}
