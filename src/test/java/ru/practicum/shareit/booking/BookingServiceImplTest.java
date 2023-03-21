package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.Range;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void addBooking() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        BookingDto addedBooking  = bookingService.addBooking(user.getId(), bookingDto);

        assertThat(addedBooking.getId(), notNullValue());
        assertThat(addedBooking.getBooker(), equalTo(user));
        assertThat(addedBooking.getStart(), equalTo(start));
        assertThat(addedBooking.getEnd(), equalTo(end));
        assertThat(addedBooking.getStatus(), equalTo(Status.WAITING));

        bookingService.setApprove(ownUser.getId(), addedBooking.getId(), true);

        ItemDto gotItemDto = itemService.getItemDto(ownUser.getId(), itemDto.getId());

        assertThat(user.getId(), equalTo(gotItemDto.getNextBooking().getBookerId()));
    }

    @Test
    void addBookingNotAvailable() {
        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", false));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().minusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        Assertions.assertEquals("Item " + itemDto.getId() + " not available!", exception.getMessage());
    }

    @Test
    void addBookingEndBefore() {
        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().minusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        Assertions.assertEquals("End date of booking 1 before now!", exception.getMessage());
    }

    @Test
    void addBookingEndBeforeStart() {
        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(3);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        Assertions.assertEquals("End date of booking 1 before start!", exception.getMessage());
    }

    @Test
    void addBookingStartBefore() {
        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().minusHours(3);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        Assertions.assertEquals("Start date of booking 1 before now!", exception.getMessage());
    }

    @Test
    void addBookingEndEqualStart() {
        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(3);
        LocalDateTime end = start;

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.addBooking(user.getId(), bookingDto));

        Assertions.assertEquals("Start for booking 1 equal end!", exception.getMessage());
    }

    @Test
    void addBookingUserEqualOwnerUser() {
        User ownerUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));

        ItemDto itemDto = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(2);
        LocalDateTime end = DateUtils.now().plusHours(3);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, ownerUser, Status.WAITING);

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.addBooking(ownerUser.getId(), bookingDto));

        Assertions.assertEquals("Not found item " + itemDto.getId(), exception.getMessage());
    }

    @Test
    void setApprove() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        BookingDto addedBooking  = bookingService.addBooking(user.getId(), bookingDto);

        BookingDto approvedBooking  = bookingService.setApprove(ownUser.getId(), addedBooking.getId(), true);

        assertThat(approvedBooking.getId(), notNullValue());
        assertThat(approvedBooking.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    void setApproveRejected() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        BookingDto addedBooking  = bookingService.addBooking(user.getId(), bookingDto);

        BookingDto approvedBooking  = bookingService.setApprove(ownUser.getId(), addedBooking.getId(), false);

        assertThat(approvedBooking.getId(), notNullValue());
        assertThat(approvedBooking.getStatus(), equalTo(Status.REJECTED));
    }

    @Test
    void setApproveAlready() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        BookingDto addedBooking  = bookingService.addBooking(user.getId(), bookingDto);

        bookingService.setApprove(ownUser.getId(), addedBooking.getId(), true);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.setApprove(ownUser.getId(), addedBooking.getId(), true));

        Assertions.assertEquals("Status booking " + addedBooking.getId() + " is bad!", exception.getMessage());
    }

    @Test
    void setApproveNotAvailable() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        BookingDto addedBooking  = bookingService.addBooking(user.getId(), bookingDto);

        itemDto.setAvailable(false);
        itemService.updateItem(ownUser.getId(), itemDto);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.setApprove(ownUser.getId(), addedBooking.getId(), true));

        Assertions.assertEquals("Item " + itemDto.getId() + " not available!", exception.getMessage());
    }

    @Test
    void setApproveUserNotEqualOwnerUser() {

        User ownerUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING);

        BookingDto addedBooking  = bookingService.addBooking(user.getId(), bookingDto);

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.setApprove(user.getId(), addedBooking.getId(), true));

        Assertions.assertEquals("Not found item " + itemDto.getId(), exception.getMessage());
    }

    @Test
    void setApproveBadId() {
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.setApprove(user.getId(), 1000L, true));

        Assertions.assertEquals("Not found booking 1000", exception.getMessage());
    }

    @Test
    void getBooking() {
        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto sourceBooking = bookingService.addBooking(user.getId(),
                new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING));

        BookingDto targetBooking = bookingService.getBooking(user.getId(), sourceBooking.getId());

        assertThat(targetBooking.getId(), notNullValue());
        assertThat(targetBooking.getStart(), equalTo(sourceBooking.getStart()));
        assertThat(targetBooking.getEnd(), equalTo(sourceBooking.getEnd()));
        assertThat(targetBooking.getStatus(), equalTo(sourceBooking.getStatus()));
    }

    @Test
    void getBookingBadId() {
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBooking(user.getId(), 1000L));

        Assertions.assertEquals("Not found booking 1000", exception.getMessage());
    }

    @Test
    void getBookingOwnerNotEqualUser() {
        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        bookingService.addBooking(user.getId(),
                new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING));

        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBooking(user.getId(), 1000L));

        Assertions.assertEquals("Not found booking 1000", exception.getMessage());
    }

    @Test
    void getBookings() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        List<BookingDto> sourceBookings = List.of(
                new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING)
        );

        for (BookingDto bookingDto : sourceBookings) {
            bookingService.addBooking(user.getId(), bookingDto);
        }

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "ALL", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsCurrent() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "CURRENT", new Range(null, null));

        assertThat(targetBookings, hasSize(0));
    }

    @Test
    void getBookingsPast() {

        User ownerUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusSeconds(1);
        LocalDateTime end = DateUtils.now().plusSeconds(2);

        BookingDto booking = bookingService.addBooking(user.getId(),
                new BookingDto(0L, start, end,
                        itemDto.getId(), itemDto, user, Status.WAITING));


        booking = bookingService.setApprove(ownerUser.getId(), booking.getId(), true);

        List<BookingDto> sourceBookings = List.of(booking);

        try {
            Thread.sleep(3000, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "PAST", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsFuture() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1, booking2, booking3);

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "FUTURE", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsWaiting() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1, booking2, booking3);

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "WAITING", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsRejected() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        booking1 = bookingService.setApprove(ownUser.getId(), booking1.getId(), false);
        booking2 = bookingService.setApprove(ownUser.getId(), booking2.getId(), true);

        List<BookingDto> sourceBookings = List.of(booking1);

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "REJECTED", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        List<BookingDto> sourceBookings = List.of(
                new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING)
        );

        for (BookingDto bookingDto : sourceBookings) {
            bookingService.addBooking(user.getId(), bookingDto);
        }

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "ALL", new Range(0, 1));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsCurrentPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "CURRENT", new Range(0, 3));

        assertThat(targetBookings, hasSize(0));
    }

    @Test
    void getBookingsPastPage() {

        User ownerUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusSeconds(1);
        LocalDateTime end = DateUtils.now().plusSeconds(2);

        BookingDto booking = bookingService.addBooking(user.getId(),
                new BookingDto(0L, start, end,
                        itemDto.getId(), itemDto, user, Status.WAITING));


        booking = bookingService.setApprove(ownerUser.getId(), booking.getId(), true);

        List<BookingDto> sourceBookings = List.of(booking);

        try {
            Thread.sleep(3000, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "PAST", new Range(0, 1));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsFuturePage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1, booking2, booking3);

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "FUTURE", new Range(0, 3));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsRejectedPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        booking1 = bookingService.setApprove(ownUser.getId(), booking1.getId(), false);
        booking2 = bookingService.setApprove(ownUser.getId(), booking2.getId(), true);

        List<BookingDto> sourceBookings = List.of(booking1);

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "REJECTED", new Range(0, 1));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsWaitingPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1);

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "WAITING", new Range(0, 1));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingsBadState() {

        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getBookings(user.getId(), "WANG", new Range(0, 1)));

        Assertions.assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getBookingsBadRange() {

        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getBookings(user.getId(), "ALL", new Range(0, 0)));

        Assertions.assertEquals("Bad range for bookings for user " + user.getId() + "!", exception.getMessage());
    }

    @Test
    void getBookingsBadRangeNull() {

        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getBookings(user.getId(), "ALL", new Range(0, null)));

        Assertions.assertEquals("Bad range for bookings for user " + user.getId() + "!", exception.getMessage());
    }

    @Test
    void getOwnerBookings() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        List<BookingDto> sourceBookings = List.of(
                new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING)
        );

        for (BookingDto bookingDto : sourceBookings) {
            bookingService.addBooking(user.getId(), bookingDto);
        }

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "ALL", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsCurrent() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "CURRENT", new Range(null, null));

        assertThat(targetBookings, hasSize(0));
    }

    @Test
    void getOwnerBookingsFuture() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1, booking2, booking3);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "FUTURE", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsWaiting() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1, booking2, booking3);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "WAITING", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsRejected() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        booking1 = bookingService.setApprove(ownUser.getId(), booking1.getId(), false);
        booking2 = bookingService.setApprove(ownUser.getId(), booking2.getId(), true);

        List<BookingDto> sourceBookings = List.of(booking1);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "REJECTED", new Range(null, null));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        List<BookingDto> sourceBookings = List.of(
                new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING)
        );

        for (BookingDto bookingDto : sourceBookings) {
            bookingService.addBooking(user.getId(), bookingDto);
        }

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "ALL", new Range(0, 1));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsCurrentPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "CURRENT", new Range(0, 3));

        assertThat(targetBookings, hasSize(0));
    }

    @Test
    void getOwnerBookingsFuturePage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1, booking2, booking3);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "FUTURE", new Range(0, 3));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsRejectedPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        booking1 = bookingService.setApprove(ownUser.getId(), booking1.getId(), false);
        booking2 = bookingService.setApprove(ownUser.getId(), booking2.getId(), true);

        List<BookingDto> sourceBookings = List.of(booking1);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "REJECTED", new Range(0, 1));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsWaitingPage() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good", true));

        BookingDto booking1 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(1), DateUtils.now().plusHours(2),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking2 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(2), DateUtils.now().plusHours(3),
                        itemDto.getId(), itemDto, user, Status.WAITING));
        BookingDto booking3 = bookingService.addBooking(user.getId(),
                new BookingDto(0L, DateUtils.now().plusHours(3), DateUtils.now().plusHours(4),
                        itemDto.getId(), itemDto, user, Status.WAITING));

        List<BookingDto> sourceBookings = List.of(booking1);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(ownUser.getId(), "WAITING", new Range(0, 1));

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("itemId", equalTo(sourceBooking.getItemId())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getOwnerBookingsBadState() {

        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getOwnerBookings(user.getId(), "WANG", new Range(0, 1)));

        Assertions.assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getOwnerBookingsBadRange() {

        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getOwnerBookings(user.getId(), "ALL", new Range(0, 0)));

        Assertions.assertEquals("Bad range for bookings for owner " + user.getId() + "!", exception.getMessage());
    }

    @Test
    void getOwnerBookingsBadRangeNull() {

        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getOwnerBookings(user.getId(), "ALL", new Range(0, null)));

        Assertions.assertEquals("Bad range for bookings for owner " + user.getId() + "!", exception.getMessage());
    }

    ItemDto makeItemDto(String name, String description, Boolean available) {
        ItemDto itemDto = new ItemDto();

        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);

        return itemDto;
    }
}