package org.example.service.booking;

import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.model.booking.BookingStatusName;
import java.util.List;

public interface BookingService {
    BookingDto createBooking(CreateBookingRequest bookingRequest);

    List<BookingDto> getMyBookings();

    List<BookingDto> getAllBookings();

    List<BookingDto> getAll(Long userId, BookingStatusName status);

    BookingDto getBookingById(Long id);

    void cancelBooking(Long id);
}
