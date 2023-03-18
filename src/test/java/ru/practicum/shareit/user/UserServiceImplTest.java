package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.UserNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
        User updatedUser = userService.getUser(user.getId());

        assertThat(updatedUser.getId(), notNullValue());
        assertThat(user.getName(), equalTo(user.getName()));
        assertThat(user.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void getUnknownUser() {
        userService.addUser(new User(0L, "Пётр", "j@j.ru"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.getUser(0L));

        Assertions.assertEquals("Not found user 0", exception.getMessage());
    }
}