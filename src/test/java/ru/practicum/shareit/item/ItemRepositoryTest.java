package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})

public class ItemRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByOwnerTest() {
        User user = userRepository.save(new User(1L, "Иван","q@q.net"));
        User ownerUser = userRepository.save(new User(0L, "Пётр", "j@j.ru"));

        Item item1 = itemRepository.save(
                new Item(0L, "Item 1", "Good", true, ownerUser, 0L));

        Item item2 = itemRepository.save(
                new Item(0L, "Item 2", "Good", true, user, 0L));

        List<Item> sourceBookings = List.of(item1);

        List<Item> items = itemRepository.findByOwner(ownerUser);

        assertThat(items, hasSize(items.size()));
        for (Item sourceItem : sourceBookings) {
            assertThat(items, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription()))
            )));
        }
    }

    @Test
    void findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTest() {
        User user = userRepository.save(new User(1L, "Иван","q@q.net"));
        User ownerUser = userRepository.save(new User(0L, "Пётр", "j@j.ru"));

        Item item1 = itemRepository.save(
                new Item(0L, "Item 1", "Good", true, ownerUser, 0L));

        Item item2 = itemRepository.save(
                new Item(0L, "Item 2", "Good", true, user, 0L));

        List<Item> sourceBookings = List.of(item1);

        List<Item> items = itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(
                "em 1", "em 1", true);

        assertThat(items, hasSize(items.size()));
        for (Item sourceItem : sourceBookings) {
            assertThat(items, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription()))
            )));
        }
    }
}
