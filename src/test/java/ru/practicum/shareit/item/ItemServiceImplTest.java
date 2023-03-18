package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
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
class ItemServiceImplTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void addItem() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        ItemDto item = itemService.addItem(user.getId(),
                new ItemDto(0L, "Дрель", "Good", false,
                        null, null, null, null));

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo("Дрель"));
        assertThat(item.getDescription(), equalTo("Good"));
    }

    @Test
    void updateItem() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        ItemDto item = itemService.addItem(user.getId(),
                new ItemDto(0L, "Дрель", "Good", false,
                        null, null, null, null));

        ItemDto updatedItem = itemService.updateItem(user.getId(),
                new ItemDto(item.getId(), "Дрель", "Bad", false,
                        null, null, null, null));

        assertThat(updatedItem.getId(), notNullValue());
        assertThat(updatedItem.getName(), equalTo("Дрель"));
        assertThat(updatedItem.getDescription(), equalTo("Bad"));
    }

    @Test
    void updateItemEmptyName() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        ItemDto item = itemService.addItem(user.getId(),
                new ItemDto(0L, "Дрель", "Good", false,
                        null, null, null, null));

        ItemDto updatedItem = itemService.updateItem(user.getId(),
                new ItemDto(item.getId(), null, "Bad", false,
                        null, null, null, null));

        assertThat(updatedItem.getId(), notNullValue());
        assertThat(updatedItem.getName(), equalTo("Дрель"));
        assertThat(updatedItem.getDescription(), equalTo("Bad"));
    }
    
    @Test
    void getItems() {
        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));

        userService.addUser(user);

        List<ItemDto> sourceItems = List.of(
                makeItemDto("Item 1", "Good"),
                makeItemDto("Item 2", "Bad"),
                makeItemDto("Item 3", "Good")
        );

        for (ItemDto itemDto : sourceItems) {
            itemService.addItem(user.getId(), itemDto);
        }

        List<ItemDto> targetItems = itemService.getItems(user.getId(), 0, 3);

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void getItem() {
        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));

        userService.addUser(user);

        ItemDto item = itemService.addItem(user.getId(), makeItemDto("Item 1", "Good"));

        ItemDto gotItem = itemService.getItemDto(user.getId(), item.getId());

        assertThat(gotItem.getId(), notNullValue());
        assertThat(gotItem.getName(), equalTo(item.getName()));
        assertThat(gotItem.getDescription(), equalTo(item.getDescription()));
    }

    @Test
    void getUnknownItem() {
        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));

        userService.addUser(user);

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.getItemDto(user.getId(), 1000L));

        Assertions.assertEquals("Not found item 1000", exception.getMessage());
    }

    @Test
    void search() {
        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));

        userService.addUser(user);

        ItemDto item1 = itemService.addItem(user.getId(), makeItemDto("Item 1", "Good"));
        ItemDto item2 = itemService.addItem(user.getId(), makeItemDto("Item 2", "Bad"));
        ItemDto item3 = itemService.addItem(user.getId(), makeItemDto("Item Bad 3", "Good"));

        List<ItemDto> sourceItems = List.of(
                item2,
                item3
        );

        List<ItemDto> targetItems = itemService.search("Bad", 0, 2);

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void addComment() {

        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        userService.addUser(user);

        User ownerUser = userService.addUser(new User(0L, "Иван", "j@y.ru"));
        userService.addUser(user);

        ItemDto item = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good"));

        LocalDateTime start = DateUtils.now().plusSeconds(1);
        LocalDateTime end = DateUtils.now().plusSeconds(2);

        BookingDto booking = bookingService.addBooking(user.getId(),
                new BookingDto(0L, start, end, item.getId(), item, user, Status.WAITING));

        bookingService.setApprove(ownerUser.getId(), booking.getId(), true);
        try {
            Thread.sleep(3000, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CommentDto commentDto = itemService.addComment(user.getId(), item.getId(),
                new CommentDto(0L, "Good", item.getId(), user.getName(), null));

        assertThat(commentDto.getId(), notNullValue());
        assertThat(commentDto.getCreated(), notNullValue());
        assertThat(commentDto.getText(), equalTo("Good"));
    }

    @Test
    void addCommentNoBookings() {

        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        userService.addUser(user);

        User ownerUser = userService.addUser(new User(0L, "Иван", "j@y.ru"));
        userService.addUser(user);

        ItemDto item = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.addComment(user.getId(), item.getId(),
                        new CommentDto(0L, "Good", item.getId(), user.getName(), null)));

        Assertions.assertEquals("No bookings!", exception.getMessage());
    }

    @Test
    void addCommentEmpty() {

        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        userService.addUser(user);

        User ownerUser = userService.addUser(new User(0L, "Иван", "j@y.ru"));
        userService.addUser(user);

        ItemDto item = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good"));

        LocalDateTime start = DateUtils.now().plusSeconds(1);
        LocalDateTime end = DateUtils.now().plusSeconds(2);

        BookingDto booking = bookingService.addBooking(user.getId(),
                new BookingDto(0L, start, end, item.getId(), item, user, Status.WAITING));

        bookingService.setApprove(ownerUser.getId(), booking.getId(), true);
        try {
            Thread.sleep(3000, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.addComment(user.getId(), item.getId(),
                        new CommentDto(0L, "", item.getId(), user.getName(), null)));

        Assertions.assertEquals("Text of comment is empty!", exception.getMessage());
    }

    @Test
    void addItemBadName() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        final NullValidationException exception = Assertions.assertThrows(
                NullValidationException.class,
                () -> itemService.addItem(user.getId(),
                        new ItemDto(0L, null, "Good", false,
                                null, null, null, null)));

        Assertions.assertEquals("Name is null!", exception.getMessage());
    }

    @Test
    void addItemBadNameBlank() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.addItem(user.getId(),
                        new ItemDto(0L, "", "Good", false,
                                null, null, null, null)));

        Assertions.assertEquals("Name is empty!", exception.getMessage());
    }

    @Test
    void updateItemForbidden() {
        User ownerUser = userService.addUser(new User(0L, "Петр", "j@j.ru"));
        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        ItemDto item = itemService.addItem(ownerUser.getId(), makeItemDto("Item 1", "Good"));

        final ForbiddenException exception = Assertions.assertThrows(
                ForbiddenException.class,
                () -> itemService.updateItem(user.getId(), item));

        Assertions.assertEquals("Another user!", exception.getMessage());
    }

    ItemDto makeItemDto(String name, String description) {
        ItemDto itemDto = new ItemDto();

        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(true);

        return itemDto;
    }
}