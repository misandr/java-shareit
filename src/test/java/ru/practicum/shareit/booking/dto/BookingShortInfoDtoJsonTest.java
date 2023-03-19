package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingShortInfoDtoJsonTest {

    @Autowired
    private JacksonTester<BookingShortInfoDto> json;

    @Test
    void testUserDto() throws Exception {
        BookingShortInfoDto bookingShortInfoDto = new BookingShortInfoDto(1L,2L);

        JsonContent<BookingShortInfoDto> result = json.write(bookingShortInfoDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(2);
    }
}