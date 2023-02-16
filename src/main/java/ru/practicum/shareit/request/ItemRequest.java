package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
public class ItemRequest {
    Integer id;
    String description;
    User requestor;
    String created;
}
