package ru.practicum.shareit.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private String name;

    @Email
    private String email;
}
