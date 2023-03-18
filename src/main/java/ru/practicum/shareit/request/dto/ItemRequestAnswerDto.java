package ru.practicum.shareit.request.dto;

import lombok.Data;

@Data
public class ItemRequestAnswerDto {
    private Long id;
    private Long ownerId;
    private String name;
}
