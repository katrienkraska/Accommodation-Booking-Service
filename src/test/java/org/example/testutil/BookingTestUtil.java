package org.example.testutil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.model.booking.Booking;
import org.example.model.booking.BookingStatusName;

public class BookingTestUtil {

    public static CreateBookingRequest createBookingRequest() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setAccommodationId(1L);
        request.setCheckInDate(LocalDate.of(2027, 1, 10));
        request.setCheckOutDate(LocalDate.of(2027, 1, 15));
        return request;
    }

    public static BookingDto createExpectedBookingDto() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setAccommodationId(1L);
        bookingDto.setUserId(2L);
        bookingDto.setCheckInDate(LocalDate.of(2027, 1, 10));
        bookingDto.setCheckOutDate(LocalDate.of(2027, 1, 15));
        bookingDto.setCreateAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        bookingDto.setStatus("PENDING");
        return bookingDto;
    }

    public static Booking getExpectedBooking() {
        Booking booking = new Booking();
        booking.setId(2L);
        booking.setCheckInDate(LocalDate.of(2026, 6, 1));
        booking.setCheckOutDate(LocalDate.of(2026, 6, 10));
        booking.setStatus(BookingStatusName.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        return booking;
    }

    public static BookingDto getBookingDto() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setCreateAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        bookingDto.setCheckInDate(LocalDate.of(2026, 6, 1));
        bookingDto.setCheckOutDate(LocalDate.of(2026, 6, 10));
        bookingDto.setAccommodationId(1L);
        bookingDto.setUserId(1L);
        return bookingDto;
    }

    public static CreateBookingRequest getBookingRequestDto() {
        CreateBookingRequest createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setAccommodationId(1L);
        createBookingRequest.setCheckInDate(LocalDate.of(2026, 6, 1));
        createBookingRequest.setCheckOutDate(LocalDate.of(2026, 6, 10));
        return createBookingRequest;
    }

    public static Booking getPendingBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(UserTestUtil.getUser());
        booking.setAccommodation(AccommodationTestUtil.getAccommodation());
        booking.setCheckInDate(LocalDate.of(2026, 6, 1));
        booking.setCheckOutDate(LocalDate.of(2026, 6, 10));
        booking.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        booking.setStatus(BookingStatusName.PENDING);
        return booking;
    }

    public static Booking getCanceledBooking() {
        Booking booking = getPendingBooking();
        booking.setStatus(BookingStatusName.CANCELED);
        return booking;
    }

    public static List<Booking> getBookingList() {
        return List.of(getPendingBooking());
    }

    public static List<BookingDto> getBookingListDto() {
        return List.of(getBookingDto());
    }

    public static BookingDto getBookingDtoCanceledStatus() {
        BookingDto bookingDto = getBookingDto();
        bookingDto.setStatus("CANCELED");
        return bookingDto;
    }
}
