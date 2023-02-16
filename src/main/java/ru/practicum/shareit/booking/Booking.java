package ru.practicum.shareit.booking;


import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class Booking {
    Integer id;
    String start;
    String end;
    Item item;
    User booker;
    String status; //— статус бронирования. Может принимать одно из следующих
}
