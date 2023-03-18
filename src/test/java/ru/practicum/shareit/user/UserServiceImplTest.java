package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NullValidationException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    private final EntityManager em;
    private final UserService userService;

    @Test
    void addUser() {
        User createdUser = new User();
        createdUser.setName("Пётр");
        createdUser.setEmail("j@j.ru");
        userService.addUser(createdUser);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", createdUser.getEmail()).getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(createdUser.getName()));
        assertThat(user.getEmail(), equalTo(createdUser.getEmail()));
    }

    @Test
    void updateUser() {
        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User updatedUser = userService.updateUser(new User(user.getId(), "Иван", "j@j1.ru"));

        assertThat(updatedUser.getId(), notNullValue());
        assertThat(updatedUser.getName(), equalTo("Иван"));
        assertThat(updatedUser.getEmail(), equalTo("j@j1.ru"));
    }

    @Test
    void getUser() {
        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User gotUser = userService.getUser(user.getId());

        assertThat(gotUser.getId(), notNullValue());
        assertThat(user.getName(), equalTo(user.getName()));
        assertThat(user.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void getUsers() {
        User user1 = userService.addUser(new User(0L, "Иван", "j@i.ru"));
        User user2 = userService.addUser(new User(0L, "Петр", "j@j.ru"));

        List<User> sourceItemUsers = List.of(user1, user2);
        List<User> targetItemUsers = userService.getUsers();

        assertThat(targetItemUsers, hasSize(sourceItemUsers.size()));
        for (User sourceItemRequest : sourceItemUsers) {
            assertThat(targetItemUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItemRequest.getName())),
                    hasProperty("email", equalTo(sourceItemRequest.getEmail()))
            )));
        }
    }

    @Test
    void getUnknownUser() {
        userService.addUser(new User(0L, "Пётр", "j@j.ru"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.getUser(0L));

        Assertions.assertEquals("Not found user 0", exception.getMessage());
    }

    @Test
    void deleteUser() {
        User user = userService.addUser(new User(0L, "Пётр", "j@j.ru"));
        User gotUser = userService.getUser(user.getId());

        assertThat(gotUser.getId(), notNullValue());
        assertThat(user.getName(), equalTo(user.getName()));
        assertThat(user.getEmail(), equalTo(user.getEmail()));

        userService.deleteUser(user.getId());

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.getUser(user.getId()));

        Assertions.assertEquals("Not found user " + user.getId(), exception.getMessage());
    }

    @Test
    void addUserBadEmail() {
        User user = new User();
        user.setName("Пётр");
        user.setEmail(null);

        final NullValidationException exception = Assertions.assertThrows(
                NullValidationException.class,
                () -> userService.addUser(user));

        Assertions.assertEquals("Email is null!", exception.getMessage());
    }

    @Test
    void addUserBadName() {
        User user = new User();
        user.setName("");
        user.setEmail("j@j.ru");

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> userService.addUser(user));

        Assertions.assertEquals("Bad name for user!", exception.getMessage());
    }

    @Test
    void deleteUserUnknownId() {
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(1000L));

        Assertions.assertEquals("Not found user 1000", exception.getMessage());
    }
}