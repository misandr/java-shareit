package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Data
public class Booking {
    private int id;
    private String start;
    private String end;
    private Item item;
    private User booker;
    private String status;
}
