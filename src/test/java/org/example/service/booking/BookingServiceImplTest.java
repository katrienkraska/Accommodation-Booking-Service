package org.example.service.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.exception.AccessDeniedException;
import org.example.exception.AccommodationUnavailableException;
import org.example.exception.BookingConflictException;
import org.example.mapper.BookingMapper;
import org.example.model.accommodation.Accommodation;
import org.example.model.booking.Booking;
import org.example.model.booking.BookingStatusName;
import org.example.model.user.User;
import org.example.repository.AccommodationRepository;
import org.example.repository.BookingRepository;
import org.example.repository.UserRepository;
import org.example.testutil.AccommodationTestUtil;
import org.example.testutil.BookingTestUtil;
import org.example.testutil.UserTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingServiceImpl;

    private Accommodation accommodation;
    private Booking pendingBooking;
    private Booking canceledBooking;
    private CreateBookingRequest createBookingRequest;
    private BookingDto bookingDto;
    private List<Booking> bookingList;
    private List<BookingDto> bookingDtoList;
    private User user;
    private User admin;
    private User stranger;

    @BeforeEach
    void setUp() {
        accommodation = AccommodationTestUtil.getAccommodation();
        pendingBooking = BookingTestUtil.getPendingBooking();
        canceledBooking = BookingTestUtil.getCanceledBooking();
        createBookingRequest = BookingTestUtil.getBookingRequestDto();
        bookingDto = BookingTestUtil.getBookingDto();
        bookingList = BookingTestUtil.getBookingList();
        bookingDtoList = BookingTestUtil.getBookingListDto();
        user = UserTestUtil.getUser();
        admin = UserTestUtil.getAdmin();
        stranger = UserTestUtil.getStranger();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User currentUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser.getEmail(), null));
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail(currentUser.getEmail()))
                .thenReturn(Optional.of(currentUser));
    }

    @Test
    @DisplayName("createBooking: valid data -> saves PENDING booking and returns dto")
    void createBooking_ValidData_ShouldReturnBookingDto() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(bookingRepository.findByAccommodationIdAndCheckOutDateAfterAndCheckInDateBefore(1L,
                createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate()))
                .thenReturn(List.of());
        authenticateAs(user);

        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto actualBooking = bookingServiceImpl.createBooking(createBookingRequest);
        assertEquals(bookingDto, actualBooking);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(accommodation, saved.getAccommodation());
        assertEquals(createBookingRequest.getCheckInDate(), saved.getCheckInDate());
        assertEquals(createBookingRequest.getCheckOutDate(), saved.getCheckOutDate());
        assertEquals(BookingStatusName.PENDING, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("createBooking: accommodation not found -> EntityNotFoundException")
    void createBooking_AccommodationNotFound_ShouldThrowException() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookingServiceImpl.createBooking(createBookingRequest));

        assertEquals("Accommodation not found", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking: availability is 0 -> AccommodationUnavailableException")
    void createBooking_NoAvailability_ShouldThrowException() {
        accommodation.setAvailability(0);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

        AccommodationUnavailableException exception = assertThrows(
                AccommodationUnavailableException.class,
                () -> bookingServiceImpl.createBooking(createBookingRequest));

        assertEquals("No available places", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking: dates overlap with existing booking -> BookingConflictException")
    void createBooking_DatesConflict_ShouldThrowException() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(bookingRepository
                .findByAccommodationIdAndCheckOutDateAfterAndCheckInDateBefore(1L,
                        createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate()))
                .thenReturn(bookingList);

        BookingConflictException exception = assertThrows(BookingConflictException.class,
                () -> bookingServiceImpl.createBooking(createBookingRequest));

        assertEquals("Accommodation is already booked for these dates", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking: current user not found -> EntityNotFoundException")
    void createBooking_UserNotFound_ShouldThrowException() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(bookingRepository.findByAccommodationIdAndCheckOutDateAfterAndCheckInDateBefore(1L,
                createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate()))
                .thenReturn(List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null));

        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookingServiceImpl.createBooking(createBookingRequest));

        assertEquals("User not found", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("getMyBookings: returns bookings of the current user")
    void getMyBookings_ShouldReturnListOfDtos() {
        authenticateAs(user);
        when(bookingRepository.findByUser(user)).thenReturn(bookingList);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actual = bookingServiceImpl.getMyBookings();

        assertEquals(bookingDtoList, actual);
    }

    @Test
    @DisplayName("getMyBookings: no bookings -> empty list")
    void getMyBookings_NoBookings_ShouldReturnEmptyList() {
        authenticateAs(user);
        when(bookingRepository.findByUser(user)).thenReturn(List.of());

        List<BookingDto> actual = bookingServiceImpl.getMyBookings();

        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("getBookingById: owner gets own booking")
    void getBookingById_Owner_ShouldReturnDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        authenticateAs(user);

        when(bookingMapper.toDto(pendingBooking)).thenReturn(bookingDto);

        BookingDto actual = bookingServiceImpl.getBookingById(1L);

        assertEquals(bookingDto, actual);
    }

    @Test
    @DisplayName("getBookingById: admin can get someone else's booking")
    void getBookingById_Admin_ShouldReturnDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        authenticateAs(admin);

        when(bookingMapper.toDto(pendingBooking)).thenReturn(bookingDto);

        BookingDto actual = bookingServiceImpl.getBookingById(1L);

        assertEquals(bookingDto, actual);
    }

    @Test
    @DisplayName("getBookingById: another non-admin user -> AccessDeniedException")
    void getBookingById_Stranger_ShouldThrowAccessDenied() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        authenticateAs(stranger);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingServiceImpl.getBookingById(1L));

        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    @DisplayName("getBookingById: booking not found -> EntityNotFoundException")
    void getBookingById_NotFound_ShouldThrowException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookingServiceImpl.getBookingById(99L));

        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    @DisplayName("getAllBookings: returns all bookings as dtos")
    void getAllBookings_ShouldReturnListOfDtos() {
        when(bookingRepository.findAll()).thenReturn(bookingList);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actual = bookingServiceImpl.getAllBookings();

        assertEquals(bookingDtoList, actual);
    }

    @Test
    @DisplayName("getAllBookings: no bookings -> empty list")
    void getAllBookings_NoBookings_ShouldReturnEmptyList() {
        when(bookingRepository.findAll()).thenReturn(List.of());

        List<BookingDto> actual = bookingServiceImpl.getAllBookings();

        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("getAll: userId and status -> findByUserIdAndStatus")
    void getAll_UserIdAndStatus_ShouldUseCombinedFilter() {
        when(bookingRepository.findByUserIdAndStatus(1L, BookingStatusName.PENDING))
                .thenReturn(bookingList);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actual = bookingServiceImpl.getAll(1L, BookingStatusName.PENDING);

        assertEquals(bookingDtoList, actual);
        verify(bookingRepository, never()).findAll();
    }

    @Test
    @DisplayName("getAll: only userId -> findByUserId")
    void getAll_OnlyUserId_ShouldFilterByUser() {
        when(bookingRepository.findByUserId(1L)).thenReturn(bookingList);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actual = bookingServiceImpl.getAll(1L, null);

        assertEquals(bookingDtoList, actual);
        verify(bookingRepository, never()).findAll();
    }

    @Test
    @DisplayName("getAll: only status -> findByStatus")
    void getAll_OnlyStatus_ShouldFilterByStatus() {
        when(bookingRepository.findByStatus(BookingStatusName.PENDING))
                .thenReturn(bookingList);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actual = bookingServiceImpl.getAll(null, BookingStatusName.PENDING);

        assertEquals(bookingDtoList, actual);
        verify(bookingRepository, never()).findAll();
    }

    @Test
    @DisplayName("getAll: no filters -> findAll")
    void getAll_NoFilters_ShouldReturnAll() {
        when(bookingRepository.findAll()).thenReturn(bookingList);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actual = bookingServiceImpl.getAll(null, null);

        assertEquals(bookingDtoList, actual);
    }

    @Test
    @DisplayName("cancelBooking: owner cancels own booking -> status CANCELED")
    void cancelBooking_Owner_ShouldSetCanceledStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        authenticateAs(user);

        bookingServiceImpl.cancelBooking(1L);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(BookingStatusName.CANCELED, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("cancelBooking: admin cancels someone else's booking")
    void cancelBooking_Admin_ShouldSetCanceledStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        authenticateAs(admin);

        bookingServiceImpl.cancelBooking(1L);

        assertEquals(BookingStatusName.CANCELED, pendingBooking.getStatus());
        verify(bookingRepository).save(pendingBooking);
    }

    @Test
    @DisplayName("cancelBooking: another non-admin user -> AccessDeniedException")
    void cancelBooking_Stranger_ShouldThrowAccessDenied() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        authenticateAs(stranger);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingServiceImpl.cancelBooking(1L));

        assertEquals("Access denied", exception.getMessage());
        assertEquals(BookingStatusName.PENDING, pendingBooking.getStatus());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("cancelBooking: booking already canceled -> BookingConflictException")
    void cancelBooking_AlreadyCanceled_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(canceledBooking));
        authenticateAs(user);

        BookingConflictException exception = assertThrows(BookingConflictException.class,
                () -> bookingServiceImpl.cancelBooking(1L));

        assertEquals("Already canceled", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("cancelBooking: booking not found -> EntityNotFoundException")
    void cancelBooking_NotFound_ShouldThrowException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookingServiceImpl.cancelBooking(99L));

        assertEquals("Booking not found", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
