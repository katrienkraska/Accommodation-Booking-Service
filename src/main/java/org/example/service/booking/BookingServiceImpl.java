package org.example.service.booking;

import jakarta.persistence.EntityNotFoundException;
import org.example.exception.AccessDeniedException;
import org.example.exception.AccommodationUnavailableException;
import org.example.exception.BookingConflictException;
import org.example.mapper.BookingMapper;
import org.example.model.booking.BookingStatusName;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.model.user.User;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.model.accommodation.Accommodation;
import org.example.model.booking.Booking;
import org.example.repository.AccommodationRepository;
import org.example.repository.BookingRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto createBooking(CreateBookingRequest bookingRequest) {

        Accommodation accommodation = accommodationRepository
                .findById(bookingRequest.getAccommodationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found"));

        if (accommodation.getAvailability() <= 0) {
            throw new AccommodationUnavailableException("No available places");
        }

        List<Booking> conflicts = bookingRepository
                .findByAccommodationIdAndCheckOutDateAfterAndCheckInDateBefore(
                        bookingRequest.getAccommodationId(),
                        bookingRequest.getCheckInDate(),
                        bookingRequest.getCheckOutDate()
                );

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(
                    "Accommodation is already booked for these dates");
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setCheckOutDate(bookingRequest.getCheckOutDate());
        booking.setStatus(BookingStatusName.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getMyBookings() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return bookingRepository.findByUser(user)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow();

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRole().name().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getAll(Long userId, BookingStatusName status) {
        List<Booking> bookings;

        if (userId != null && status != null) {
            bookings = bookingRepository.findByUserIdAndStatus(userId, status);
        } else if (userId != null) {
            bookings = bookingRepository.findByUserId(userId);
        } else if (status != null) {
            bookings = bookingRepository.findByStatus(status);
        } else {
            bookings = bookingRepository.findAll();
        }
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow();

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRole().name().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        if (booking.getStatus() == BookingStatusName.CANCELED) {
            throw new BookingConflictException("Already canceled");
        }

        booking.setStatus(BookingStatusName.CANCELED);

        bookingRepository.save(booking);
    }
}
