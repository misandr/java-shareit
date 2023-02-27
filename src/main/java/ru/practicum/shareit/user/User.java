package ru.practicum.shareit.user;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class User {
    private int id;
    private String name;
    @Email
    private String email;
}
