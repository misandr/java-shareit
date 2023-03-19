//package ru.practicum.shareit.booking;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import ru.practicum.shareit.DateUtils;
//import ru.practicum.shareit.booking.dto.BookingDto;
//import ru.practicum.shareit.booking.model.Booking;
//import ru.practicum.shareit.booking.model.enums.Status;
//import ru.practicum.shareit.item.ItemMapper;
//import ru.practicum.shareit.item.dto.ItemDto;
//import ru.practicum.shareit.item.model.Item;
//import ru.practicum.shareit.user.User;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest
//public class BookingMapperTest {
//
//    @Test
//    public void toBooking() {
//
//        LocalDateTime start = DateUtils.now().plusHours(1);
//        LocalDateTime end = DateUtils.now().plusHours(2);
//
//        BookingDto bookingDto = new BookingDto(1L, start, end, 2L, null, null, Status.WAITING);
//
//        Booking booking = BookingMapper.toBooking(bookingDto);
//
//        assertEquals(booking.getId(), bookingDto.getId());
//        assertEquals(booking.getStart(), bookingDto.getStart());
//        assertEquals(booking.getEnd(), bookingDto.getEnd());
//        assertEquals(booking.getStatus(), bookingDto.getStatus());
//    }
//
//    @Test
//    public void toBookingDto() {
//        User user = new User(1L, "Иван", "j@i.ru");
//        Item item = new Item(1L, "Дрель", "Good", false, user, null);
//
//        LocalDateTime start = DateUtils.now().plusHours(1);
//        LocalDateTime end = DateUtils.now().plusHours(2);
//
//        Booking booking = new Booking(1L, start, end, item, user, Status.WAITING);
//        BookingDto bookingDto = BookingMapper.toBookingDto(booking, ItemMapper.toItemDto(item));
//
//        assertEquals(booking.getId(), bookingDto.getId());
//        assertEquals(booking.getStart(), bookingDto.getStart());
//        assertEquals(booking.getEnd(), bookingDto.getEnd());
//        assertEquals(booking.getStatus(), bookingDto.getStatus());
//    }
//
////    public static List<BookingDto> toListBookingDto(List<Booking> bookings) {
////        List<BookingDto> bookingsDto = new ArrayList<>();
////
////        for (Booking booking : bookings) {
////            Item item = booking.getItem();
////            ItemDto itemDto = ItemMapper.toItemDto(item);
////            bookingsDto.add(BookingMapperTest.toBookingDto(booking, itemDto));
////        }
////
////        return bookingsDto;
////    }
//}
