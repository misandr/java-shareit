package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {

    private final UserService userService;
    private final ItemRequestService itemRequestService;

    @Test
    void addItemRequest() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        ItemRequestDto itemRequest = itemRequestService.addItemRequest(user.getId(),
                new ItemRequestDto(0L, "Дрель", null, null));

        ItemRequestDto addedItemRequest = itemRequestService.addItemRequest(user.getId(), itemRequest);

        assertThat(addedItemRequest.getId(), notNullValue());
        assertThat(addedItemRequest.getDescription(), equalTo(itemRequest.getDescription()));
    }

    @Test
    void getOwnItemRequests() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        ItemRequestDto itemRequest = itemRequestService.addItemRequest(user.getId(),
                new ItemRequestDto(0L, "Дрель", null, null));

        List<ItemRequestDto> sourceItemRequests = List.of(itemRequest);

        List<ItemRequestDto> targetItemRequests = itemRequestService.getOwnItemRequests(user.getId());

        assertThat(targetItemRequests, hasSize(sourceItemRequests.size()));
        for (ItemRequestDto sourceItemRequest : sourceItemRequests) {
            assertThat(targetItemRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItemRequest.getDescription())),
                    hasProperty("created", notNullValue())
            )));
        }
    }

    @Test
    void getOtherItemRequests() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));
        User otherUser = userService.addUser(new User(0L, "Петр", "j@j.ru"));

        ItemRequestDto itemRequest = itemRequestService.addItemRequest(otherUser.getId(),
                new ItemRequestDto(0L, "Дрель", null, null));

        List<ItemRequestDto> sourceItemRequests = List.of(itemRequest);

        List<ItemRequestDto> targetItemRequests = itemRequestService.getOtherItemRequests(user.getId(), 0, 1);

        assertThat(targetItemRequests, hasSize(sourceItemRequests.size()));
        for (ItemRequestDto sourceItemRequest : sourceItemRequests) {
            assertThat(targetItemRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItemRequest.getDescription())),
                    hasProperty("created", notNullValue())
            )));
        }
    }

    @Test
    void getItemRequestDto() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        ItemRequestDto itemRequest = itemRequestService.addItemRequest(user.getId(),
                new ItemRequestDto(0L, "Дрель", null, null));

        ItemRequestDto addedItemRequest = itemRequestService.addItemRequest(user.getId(), itemRequest);

        ItemRequestDto gotItemRequest = itemRequestService.getItemRequestDto(user.getId(), addedItemRequest.getId());

        assertThat(addedItemRequest.getId(), notNullValue());
        assertThat(addedItemRequest.getDescription(), equalTo(gotItemRequest.getDescription()));
    }

    @Test
    void getUnknownItemRequestDto() {
        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        final ItemRequestNotFoundException exception = Assertions.assertThrows(
                ItemRequestNotFoundException.class,
                () -> itemRequestService.getItemRequestDto(user.getId(), 1000L));

        Assertions.assertEquals("Not found request 1000", exception.getMessage());
    }

    @Test
    void addItemRequestDescriptionNull() {
        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        final NullValidationException exception = Assertions.assertThrows(
                NullValidationException.class,
                () -> itemRequestService.addItemRequest(user.getId(),
                        new ItemRequestDto(0L, null, null, null)));

        Assertions.assertEquals("Description is null!", exception.getMessage());
    }

    @Test
    void getOwnItemRequestsBadRange() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        List<ItemRequestDto> targetItemRequests = itemRequestService.getOwnItemRequests(user.getId());

        assertThat(targetItemRequests, hasSize(0));
    }

    @Test
    void getOtherItemRequestsBadRange() {

        User user = userService.addUser(new User(0L, "Иван", "j@i.ru"));

        List<ItemRequestDto> targetItemRequests = itemRequestService.getOtherItemRequests(user.getId(), 0, null);

        assertThat(targetItemRequests, hasSize(0));
    }
}