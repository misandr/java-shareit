package ru.practicum.shareit;

import lombok.Data;

@Data
public class Range {
    private final Integer from;
    private final Integer size;

    private Range(Integer from, Integer size) {
        this.from = from;
        this.size = size;
    }

    public static Range of(Integer from, Integer size) {
        return new Range(from, size);
    }
}
