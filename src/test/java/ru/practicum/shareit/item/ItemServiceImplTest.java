package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
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
class ItemServiceImplTest {

    private final ItemService itemService;
    private final UserService userService;

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

    ItemDto makeItemDto(String name, String description) {
        ItemDto itemDto = new ItemDto();

        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(true);

        return itemDto;
    }
}