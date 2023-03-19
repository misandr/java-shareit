package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
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


@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void addItemRequest() throws Exception {
        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime created = DateUtils.now();
        ItemRequestDto itemRequestDto = createItemRequestDto("Дрель", created, List.of(itemDto));

        when(itemRequestService.addItemRequest(any(), any())).thenReturn(itemRequestDto);

        mvc.perform(post("/requests").header(HEADER_USER_ID, 1).content(mapper.writeValueAsString(itemRequestDto)).characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class)).andExpect(jsonPath("$.description", is(itemRequestDto.getDescription()))).andExpect(jsonPath("$.items.[0].id", is(itemDto.getId().intValue()))).andExpect(jsonPath("$.items.[0].name", is(itemDto.getName())));
    }

    @Test
    void getOwnItemRequests() throws Exception {

        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime created = DateUtils.now();
        ItemRequestDto itemRequestDto = createItemRequestDto("Дрель", created, List.of(itemDto));

        when(itemRequestService.getOwnItemRequests(any())).thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests").header(HEADER_USER_ID, 1).characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class)).andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription()))).andExpect(jsonPath("$.[0].items.[0].id", is(itemDto.getId().intValue()))).andExpect(jsonPath("$.[0].items.[0].name", is(itemDto.getName())));
    }

    @Test
    void getOtherItemRequests() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime created = DateUtils.now();
        ItemRequestDto itemRequestDto = createItemRequestDto("Дрель", created, List.of(itemDto));

        when(itemRequestService.getOtherItemRequests(any(), any(), any())).thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all").header(HEADER_USER_ID, 1).param("from", "0").param("size", "1").characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class)).andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription()))).andExpect(jsonPath("$.[0].items.[0].id", is(itemDto.getId().intValue()))).andExpect(jsonPath("$.[0].items.[0].name", is(itemDto.getName())));
    }

    @Test
    void getItemRequest() throws Exception {
        User user = createUser("Пётр");

        ItemDto itemDto = createItemDto("Вещь");

        LocalDateTime created = DateUtils.now();
        ItemRequestDto itemRequestDto = createItemRequestDto("Дрель", created, List.of(itemDto));

        when(itemRequestService.getItemRequestDto(any(), any())).thenReturn(itemRequestDto);

        mvc.perform(get("/requests/1").header(HEADER_USER_ID, 1).characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class)).andExpect(jsonPath("$.description", is(itemRequestDto.getDescription()))).andExpect(jsonPath("$.items[0].id", is(itemDto.getId().intValue()))).andExpect(jsonPath("$.items[0].name", is(itemDto.getName())));
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

    ItemRequestDto createItemRequestDto(String description, LocalDateTime created, List<ItemDto> items) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription(description);
        itemRequestDto.setCreated(created);
        itemRequestDto.setItems(items);
        return itemRequestDto;
    }

    User createUser(String name) {
        User user = new User();
        user.setId(1L);
        user.setName(name);
        user.setEmail("j@j.ru");
        return user;
    }
}