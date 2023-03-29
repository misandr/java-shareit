package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testUserDto() throws Exception {
        CommentDto commentDto = new CommentDto(
                1L,
                "удобная",
                2L,
                "Ваня",
                null);

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("удобная");
        assertThat(result).extractingJsonPathNumberValue("$.item").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Ваня");
    }
}