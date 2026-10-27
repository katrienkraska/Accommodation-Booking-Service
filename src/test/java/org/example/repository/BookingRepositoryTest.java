package org.example.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import lombok.SneakyThrows;
import org.example.model.booking.Booking;
import org.example.model.booking.BookingStatusName;
import org.example.testutil.BookingTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    private static final String DELETE_ALL_SQL =
            "delete/delete-all.sql";

    public static final String INSERT_BOOKINGS_SQL =
            "database/booking/insert-bookings.sql";

    private static final String INSERT_ACCOMMODATION_SQL =
            "database/accommodation/insert-accommodation.sql";

    private static final String INSERT_USERS_SQL =
            "database/user/insert-users.sql";

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) throws SQLException {
        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_USERS_SQL));

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_ACCOMMODATION_SQL));

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_BOOKINGS_SQL));
        }
    }

    @Test
    @DisplayName("Should return bookings by user id")
    void findByUserId_ValidUserId_ShouldReturnBookings() {
        Long userId = 2L;

        List<Booking> actual = bookingRepository.findByUserId(userId);

        assertFalse(actual.isEmpty());
        assertTrue(actual.stream().allMatch(
                booking -> booking.getUser().getId().equals(userId)));
    }

    @Test
    @DisplayName("Should return bookings by status")
    void findByStatus_ValidStatus_ShouldReturnBookings() {

        Booking expectedBooking = BookingTestUtil.getExpectedBooking();

        List<Booking> actual = bookingRepository.findByStatus(BookingStatusName.CONFIRMED);

        Booking actualBooking = actual.get(0);

        assertEquals(expectedBooking.getCheckInDate(), actualBooking.getCheckInDate());
        assertEquals(expectedBooking.getCheckOutDate(), actualBooking.getCheckOutDate());
        assertEquals(expectedBooking.getStatus(), actualBooking.getStatus());
    }

    @Test
    @DisplayName("Should return overlapping bookings count")
    void isDatesAvailableForAccommodation_ValidDates_ShouldReturnCount() {

        Long accommodationId = 1L;

        Long actual = bookingRepository.isDatesAvailableForAccommodation(
                accommodationId,
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 10));

        Long expected = 1L;

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should return bookings by check in date and status")
    void findAllByCheckInDate_ValidDateAndStatus_ShouldReturnBookings() {

        Booking expectedBooking = BookingTestUtil.getExpectedBooking();

        List<Booking> actual = bookingRepository.findAllByCheckInDate(
                expectedBooking.getCheckInDate(), BookingStatusName.CONFIRMED);

        Booking actualBooking = actual.get(0);

        assertEquals(expectedBooking.getCheckInDate(), actualBooking.getCheckInDate());
        assertEquals(expectedBooking.getStatus(), actualBooking.getStatus());
    }

    @Test
    @DisplayName("Should return bookings created before date and status")
    void findAllByCreatedAtAndStatus_ValidData_ShouldReturnBookings() {

        Booking expectedBooking = BookingTestUtil.getExpectedBooking();

        List<Booking> actual = bookingRepository.findAllByCreatedAtAndStatus(
                LocalDateTime.now().plusDays(1), BookingStatusName.CONFIRMED);

        Booking actualBooking = actual.get(0);

        assertEquals(expectedBooking.getCheckInDate(), actualBooking.getCheckInDate());
        assertEquals(expectedBooking.getStatus(), actualBooking.getStatus());
    }

    @Test
    @DisplayName("Should return bookings by check out date and status")
    void findByCheckOutDateAndStatus_ValidData_ShouldReturnBookings() {
        Booking expectedBooking = BookingTestUtil.getExpectedBooking();

        List<Booking> actual = bookingRepository.findByCheckOutDateAndStatus(
                expectedBooking.getCheckOutDate(), BookingStatusName.CONFIRMED);

        Booking actualBooking = actual.get(0);

        assertEquals(expectedBooking.getCheckOutDate(), actualBooking.getCheckOutDate());
        assertEquals(expectedBooking.getStatus(), actualBooking.getStatus());
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(DELETE_ALL_SQL));
        }
    }
}
