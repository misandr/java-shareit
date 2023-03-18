package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.enums.Status;
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
    void getBookings() {

        User ownUser = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@j1.ru"));

        ItemDto itemDto = itemService.addItem(ownUser.getId(), makeItemDto("Item 1", "Good"));

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        List<BookingDto> sourceBookings = List.of(
                new BookingDto(1L, start, end, itemDto.getId(), itemDto, user, Status.WAITING)
        );

        for (BookingDto bookingDto : sourceBookings) {
            bookingService.addBooking(user.getId(), bookingDto);
        }

        List<BookingDto> targetBookings = bookingService.getBookings(user.getId(), "ALL", 0, 1);

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

    ItemDto makeItemDto(String name, String description) {
        ItemDto itemDto = new ItemDto();

        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(true);

        return itemDto;
    }
}