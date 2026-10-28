package org.example.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.example.model.payment.Payment;
import org.example.model.payment.PaymentStatus;
import org.example.testutil.PaymentTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    private static final String DELETE_ALL_SQL =
            "delete/delete-all.sql";

    public static final String INSERT_BOOKINGS_SQL =
            "database/booking/insert-bookings.sql";

    private static final String INSERT_ACCOMMODATION_SQL =
            "database/accommodation/insert-accommodation.sql";

    private static final String PAYMENT_INSERT_PAYMENTS_SQL =
            "database/payment/insert-payments.sql";

    private static final String INSERT_USERS_SQL =
            "database/user/insert-users.sql";

    private static final PaymentStatus.PaymentStatusName PENDING_PAYMENT_STATUS =
            PaymentStatus.PaymentStatusName.PENDING;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) throws SQLException {

        tearDown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_USERS_SQL));

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_ACCOMMODATION_SQL));

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_BOOKINGS_SQL));

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(PAYMENT_INSERT_PAYMENTS_SQL));
        }
    }

    @Test
    @DisplayName("Should return payment when booking ID exists")
    void findPaymentByBookingId_ExistingBookingId_ShouldReturnPayment() {

        Payment expectedPayment = PaymentTestUtil.getExpectedPayment();

        Optional<Payment> actualResult = paymentRepository.findPaymentByBookingId(1L);
        assertTrue(actualResult.isPresent());

        Payment actualPayment = actualResult.get();

        assertEquals(expectedPayment.getId(), actualPayment.getId());
        assertEquals(expectedPayment.getSessionId(), actualPayment.getSessionId());
        assertEquals(expectedPayment.getSessionUrl(), actualPayment.getSessionUrl());
        assertEquals(expectedPayment.getAmountToPay(), actualPayment.getAmountToPay());
    }

    @Test
    @DisplayName("Should return payment by session ID")
    void findPaymentBySessionId_ExistingSessionId_ShouldReturnPayment() {

        Payment expectedPayment = PaymentTestUtil.getExpectedPayment();

        String sessionId = "testSessionId1";

        Optional<Payment> actualResult = paymentRepository.findPaymentBySessionId(sessionId);
        assertTrue(actualResult.isPresent());

        Payment actualPayment = actualResult.get();

        assertEquals(expectedPayment.getId(), actualPayment.getId());
        assertEquals(expectedPayment.getSessionId(), actualPayment.getSessionId());
        assertEquals(expectedPayment.getSessionUrl(), actualPayment.getSessionUrl());
        assertEquals(expectedPayment.getAmountToPay(), actualPayment.getAmountToPay());
    }

    @Test
    @DisplayName("Should return list of payments by user email")
    void findPaymentByBookingUserEmail_ValidEmail_ShouldReturnPayments() {

        String userEmail = "user@example.com";
        int expectPayments = 2;

        List<Payment> bookingUserEmail = paymentRepository.findPaymentByBookingUserEmail(userEmail);

        assertEquals(expectPayments, bookingUserEmail.size());
    }

    @Test
    @DisplayName("Should return payments by booking IDs and payment status")
    void findAllByBookingIdInAndStatusName_ValidInput_ShouldReturnPayments() {

        List<Long> existingBookingIds = List.of(1L, 2L);

        List<Payment> actualResult = paymentRepository
                .findAllByBookingIdInAndStatus_Name(existingBookingIds, PENDING_PAYMENT_STATUS);

        assertEquals(1, actualResult.size());
        assertEquals(2, actualResult.get(0).getBooking().getId());
    }

    @AfterEach
    void tearDown(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection, new ClassPathResource(DELETE_ALL_SQL));
        }
    }
}
