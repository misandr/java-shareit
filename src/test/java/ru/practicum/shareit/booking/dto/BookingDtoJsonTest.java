package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.DateUtils;
import ru.practicum.shareit.booking.model.enums.Status;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testUserDto() throws Exception {
        BookingDto bookingDto = new BookingDto(
                1L,
                DateUtils.now().plusHours(1),
                DateUtils.now().plusHours(2),
                2L,
                null,
                null,
                Status.WAITING);

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }
}