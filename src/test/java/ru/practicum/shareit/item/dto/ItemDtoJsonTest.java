package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testUserDto() throws Exception {
        ItemDto itemDto = new ItemDto(
                1L,
                "вещь",
                "удобная",
                true,
                null,
                null,
                null,
                null);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("вещь");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("удобная");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
    }
}