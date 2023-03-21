package ru.practicum.shareit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Range {
    Integer from;
    Integer size;

    public boolean isPresent() {
        if ((from == null) && (size == null)) {
            return false;
        }

        return true;
    }

    public boolean isWrong() {
        if ((from != null) && (size != null)) {
            if ((from < 0) || (size <= 0))
                return true;
        } else {
            return true;
        }

        return false;
    }
}
