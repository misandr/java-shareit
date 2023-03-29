package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.enums.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.Constants.HEADER_USER_ID;
import static ru.practicum.shareit.booking.model.enums.Status.APPROVED;
import static ru.practicum.shareit.booking.model.enums.Status.WAITING;


@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void addBooking() throws Exception {

        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, WAITING);

        when(bookingService.addBooking(any(), any()))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .header(HEADER_USER_ID, 1)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.booker.name", is(user.getName())));
    }

    @Test
    void setApprove() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, APPROVED);

        when(bookingService.setApprove(any(), any(), any()))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/1")
                        .header(HEADER_USER_ID, 1)
                        .param("approved", String.valueOf(true))
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.booker.name", is(user.getName())));
    }

    @Test
    void getBooking() throws Exception {

        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, APPROVED);

        when(bookingService.getBooking(1L, 1L))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/1")
                        .header(HEADER_USER_ID, 1)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.booker.name", is(user.getName())));
    }

    @Test
    void getBookingsByState() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, APPROVED);

        when(bookingService.getBookings(any(), any(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings")
                        .header(HEADER_USER_ID, 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.[0].item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.[0].item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.[0].booker.name", is(user.getName())));
    }

    @Test
    void getBookingsByStateNull() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, APPROVED);

        when(bookingService.getBookings(any(), any(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings")
                        .header(HEADER_USER_ID, 1)
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.[0].item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.[0].item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.[0].booker.name", is(user.getName())));
    }

    @Test
    void getBookings() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, APPROVED);

        when(bookingService.getBookings(any(), any(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings")
                        .header(HEADER_USER_ID, 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.[0].item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.[0].item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.[0].booker.name", is(user.getName())));
    }

    @Test
    void getBookingsByOwner() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, APPROVED);

        when(bookingService.getOwnerBookings(any(), any(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .header(HEADER_USER_ID, 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.[0].item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.[0].item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.[0].booker.name", is(user.getName())));
    }

    @Test
    void getBookingsByOwnerByStateNull() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime start = DateUtils.now().plusHours(1);
        LocalDateTime end = DateUtils.now().plusHours(2);

        BookingDto bookingDto = createBookingDto(start, end, itemDto, user, APPROVED);

        when(bookingService.getOwnerBookings(any(), any(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .header(HEADER_USER_ID, 1)
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.[0].item.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.[0].item.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].item.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].booker.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.[0].booker.name", is(user.getName())));
    }

    BookingDto createBookingDto(LocalDateTime start, LocalDateTime end, ItemDto item, User booker, Status status) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItem(item);
        bookingDto.setBooker(booker);
        bookingDto.setStatus(status);
        return bookingDto;
    }

    ItemDto createItemDto(String name) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName(name);
        itemDto.setDescription("Нужная вещь");
        itemDto.setAvailable(false);
        return itemDto;
    }

    User createUser(String name) {
        User user = new User();
        user.setId(1L);
        user.setName(name);
        user.setEmail("j@j.ru");
        return user;
    }
}