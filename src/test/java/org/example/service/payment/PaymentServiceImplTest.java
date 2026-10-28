package org.example.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.example.dto.payment.PaymentDetailsDto;
import org.example.dto.payment.PaymentDto;
import org.example.exception.EntityNotFoundException;
import org.example.exception.PaymentNotConfirmedException;
import org.example.exception.StripePaymentSessionException;
import org.example.exception.UnauthorizedBookingAccessException;
import org.example.mapper.BookingMapper;
import org.example.mapper.PaymentMapper;
import org.example.model.booking.Booking;
import org.example.model.booking.BookingStatusName;
import org.example.model.payment.Payment;
import org.example.model.payment.PaymentStatus;
import org.example.model.payment.PaymentStatus.PaymentStatusName;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.PaymentStatusRepository;
import org.example.telegram.BookingNotificationBot;
import org.example.telegram.TelegramNotificationBuilder;
import org.example.testutil.BookingTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String SUCCESS_URL = "http://localhost/payments/success";
    private static final String CANCEL_URL = "http://localhost/payments/cancel";
    private static final String SESSION_ID = "sess_1";
    private static final String SESSION_URL = "https://checkout.stripe.com/sess_1";
    private static final String EMAIL = "test@gmail.com";
    private static final long BOOKING_ID = 1L;
    private static final long AMOUNT_TOTAL = 1395L;

    @Mock
    private BookingNotificationBot bookingNotificationBot;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStatusRepository paymentStatusRepository;

    @Mock
    private Session session;

    private PaymentServiceImpl paymentService;
    private Booking booking;
    private PaymentStatus pendingStatus;
    private PaymentStatus paidStatus;
    private PaymentStatus canceledStatus;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(SUCCESS_URL, CANCEL_URL, bookingNotificationBot,
                paymentMapper, bookingMapper, bookingRepository, paymentRepository, paymentStatusRepository);

        booking = BookingTestUtil.getPendingBooking();
        pendingStatus = buildStatus(1L, PaymentStatusName.PENDING);
        paidStatus = buildStatus(2L, PaymentStatusName.PAID);
        canceledStatus = buildStatus(3L, PaymentStatusName.CANCELED);
    }

    @Test
    @DisplayName("createPaymentCheckoutSession: valid booking -> saves PENDING payment, "
            + "notifies user and returns session url")
    void createPaymentCheckoutSession_ValidBooking_ShouldReturnSessionUrl() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(paymentRepository.findPaymentByBookingId(BOOKING_ID)).thenReturn(Optional.empty());
        when(bookingMapper.getBookingDescription(booking)).thenReturn("description");
        when(session.getUrl()).thenReturn(SESSION_URL);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getAmountTotal()).thenReturn(AMOUNT_TOTAL);
        when(paymentStatusRepository.findPaymentStatusByName(PaymentStatusName.PENDING))
                .thenReturn(pendingStatus);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class);
             MockedStatic<TelegramNotificationBuilder> telegramStatic =
                     mockStatic(TelegramNotificationBuilder.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(session);

            telegramStatic.when(() -> TelegramNotificationBuilder.paymentInitiated(any(Payment.class)))
                    .thenReturn("payment initiated");

            String actual = paymentService.createPaymentCheckoutSession(BOOKING_ID, EMAIL);

            assertEquals(SESSION_URL, actual);
        }

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals(SESSION_ID, saved.getSessionId());
        assertEquals(SESSION_URL, saved.getSessionUrl());
        assertEquals(booking, saved.getBooking());
        assertEquals(pendingStatus, saved.getStatus());
        assertEquals(BigDecimal.valueOf(AMOUNT_TOTAL), saved.getAmountToPay());
        verify(bookingNotificationBot).sendMessage(EMAIL, "payment initiated");
    }

    @Test
    @DisplayName("createPaymentCheckoutSession: booking not found -> EntityNotFoundException")
    void createPaymentCheckoutSession_BookingNotFound_ShouldThrowException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPaymentCheckoutSession(99L, EMAIL));

        assertEquals("Cant find booking id 99", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("createPaymentCheckoutSession: booking of another user "
            + "-> UnauthorizedBookingAccessException")
    void createPaymentCheckoutSession_WrongUser_ShouldThrowException() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        UnauthorizedBookingAccessException exception = assertThrows(
                UnauthorizedBookingAccessException.class,
                () -> paymentService.createPaymentCheckoutSession(
                        BOOKING_ID, "other@gmail.com"));

        assertEquals("User with email other@gmail.com cant have permission "
                + "to booking with id. 1", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("createPaymentCheckoutSession: PENDING payment already exists "
            + "-> returns its url without creating new session")
    void createPaymentCheckoutSession_PendingPaymentExists_ShouldReturnExistingUrl() {
        Payment existing = buildPayment(pendingStatus);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(paymentRepository.findPaymentByBookingId(BOOKING_ID)).thenReturn(Optional.of(existing));

        String actual = paymentService.createPaymentCheckoutSession(BOOKING_ID, EMAIL);

        assertEquals(SESSION_URL, actual);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("createPaymentCheckoutSession: Stripe fails to create session "
            + "-> StripePaymentSessionException")
    void createPaymentCheckoutSession_StripeFails_ShouldThrowException() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(paymentRepository.findPaymentByBookingId(BOOKING_ID)).thenReturn(Optional.empty());
        when(bookingMapper.getBookingDescription(booking)).thenReturn("description");

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(stripeException());

            StripePaymentSessionException exception = assertThrows(StripePaymentSessionException.class,
                    () -> paymentService.createPaymentCheckoutSession(BOOKING_ID, EMAIL));

            assertTrue(exception.getMessage().startsWith("Failed to create Stripe Checkout session. "));
        }
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("successPayment: payment not found -> EntityNotFoundException")
    void successPayment_PaymentNotFound_ShouldThrowException() {
        when(paymentRepository.findPaymentBySessionId(SESSION_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.successPayment(SESSION_ID));

        assertEquals("Payment not found by id " + SESSION_ID, exception.getMessage());
    }

    @Test
    @DisplayName("successPayment: Stripe session not paid -> PaymentNotConfirmedException")
    void successPayment_SessionNotPaid_ShouldThrowException() {
        Payment payment = buildPayment(pendingStatus);

        when(paymentRepository.findPaymentBySessionId(SESSION_ID)).thenReturn(Optional.of(payment));
        when(session.getPaymentStatus()).thenReturn("unpaid");

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve(anyString())).thenReturn(session);

            PaymentNotConfirmedException exception = assertThrows(PaymentNotConfirmedException.class,
                    () -> paymentService.successPayment(SESSION_ID));

            assertEquals("Current payment status is unpaid", exception.getMessage());
        }
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("successPayment: session is paid -> booking CONFIRMED, payment PAID, "
            + "two notifications sent")
    void successPayment_SessionPaid_ShouldConfirmBookingAndPayment() {
        Payment payment = buildPayment(pendingStatus);
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(1L);
        paymentDto.setSessionId(SESSION_ID);

        when(paymentRepository.findPaymentBySessionId(SESSION_ID)).thenReturn(Optional.of(payment));
        when(session.getPaymentStatus()).thenReturn("paid");
        when(paymentStatusRepository.findPaymentStatusByName(PaymentStatusName.PAID))
                .thenReturn(paidStatus);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class);
             MockedStatic<TelegramNotificationBuilder> telegramStatic =
                     mockStatic(TelegramNotificationBuilder.class)) {
            sessionStatic.when(() -> Session.retrieve(anyString())).thenReturn(session);
            telegramStatic.when(() -> TelegramNotificationBuilder
                    .paymentSuccessful(any(Payment.class))).thenReturn("payment successful");
            telegramStatic.when(() -> TelegramNotificationBuilder.bookingConfirmed(any(Booking.class)))
                    .thenReturn("booking confirmed");

            PaymentDto actual = paymentService.successPayment(SESSION_ID);

            assertSame(paymentDto, actual);
        }
        assertEquals(BookingStatusName.CONFIRMED, booking.getStatus());
        assertEquals(paidStatus, payment.getStatus());

        verify(bookingRepository).save(booking);
        verify(paymentRepository).save(payment);
        verify(bookingNotificationBot).sendMessage(EMAIL, "payment successful");
        verify(bookingNotificationBot).sendMessage(EMAIL, "booking confirmed");
    }

    @Test
    @DisplayName("cancelPaymentAndBooking: payment is not PENDING "
            + "-> StripePaymentSessionException")
    void cancelPaymentAndBooking_PaymentNotPending_ShouldThrowException() {
        Payment payment = buildPayment(paidStatus);
        when(paymentRepository.findPaymentBySessionId(SESSION_ID)).thenReturn(Optional.of(payment));

        StripePaymentSessionException exception = assertThrows(StripePaymentSessionException.class,
                () -> paymentService.cancelPaymentAndBooking(SESSION_ID));

        assertTrue(exception.getMessage().startsWith("Cant cancel stripe session 1"));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("cancelPaymentAndBooking: PENDING payment -> statuses CANCELED, "
            + "Stripe session expired, two notifications sent")
    void cancelPaymentAndBooking_PendingPayment_ShouldCancelEverything() throws Exception {
        Payment payment = buildPayment(pendingStatus);
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(1L);
        paymentDto.setSessionId(SESSION_ID);

        when(paymentRepository.findPaymentBySessionId(SESSION_ID))
                .thenReturn(Optional.of(payment));
        when(paymentStatusRepository.findPaymentStatusByName(PaymentStatusName.CANCELED))
                .thenReturn(canceledStatus);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class);
             MockedStatic<TelegramNotificationBuilder> telegramStatic =
                     mockStatic(TelegramNotificationBuilder.class)) {
            sessionStatic.when(() -> Session.retrieve(anyString())).thenReturn(session);
            telegramStatic.when(() -> TelegramNotificationBuilder.paymentCancelled(any(Payment.class)))
                    .thenReturn("payment cancelled");
            telegramStatic.when(() -> TelegramNotificationBuilder.bookingCancelled(any(Booking.class)))
                    .thenReturn("booking cancelled");

            PaymentDto actual = paymentService.cancelPaymentAndBooking(SESSION_ID);

            assertSame(paymentDto, actual);
        }
        assertEquals(canceledStatus, payment.getStatus());
        assertEquals(BookingStatusName.CANCELED, booking.getStatus());

        verify(session).expire();
        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(booking);
        verify(bookingNotificationBot).sendMessage(EMAIL, "payment cancelled");
        verify(bookingNotificationBot).sendMessage(EMAIL, "booking cancelled");
    }

    @Test
    @DisplayName("findPaymentsByUserEmail: returns payments of the user as details dtos")
    void findPaymentsByUserEmail_ShouldReturnListOfDetailsDtos() {
        Payment payment = buildPayment(paidStatus);
        PaymentDetailsDto detailsDto = new PaymentDetailsDto();
        detailsDto.setId(1L);
        detailsDto.setBookingId(BOOKING_ID);

        when(paymentRepository.findPaymentByBookingUserEmail(EMAIL)).thenReturn(List.of(payment));
        when(paymentMapper.toDetailsDto(payment)).thenReturn(detailsDto);

        List<PaymentDetailsDto> actual = paymentService.findPaymentsByUserEmail(EMAIL);

        assertEquals(List.of(detailsDto), actual);
    }

    @Test
    @DisplayName("findAllPayments: returns all payments as details dtos")
    void findAllPayments_ShouldReturnListOfDetailsDtos() {
        Payment first = buildPayment(paidStatus);
        Payment second = buildPayment(pendingStatus);
        PaymentDetailsDto firstDto = new PaymentDetailsDto();
        firstDto.setId(1L);
        PaymentDetailsDto secondDto = new PaymentDetailsDto();
        secondDto.setId(2L);

        when(paymentRepository.findAll()).thenReturn(List.of(first, second));
        when(paymentMapper.toDetailsDto(first)).thenReturn(firstDto);
        when(paymentMapper.toDetailsDto(second)).thenReturn(secondDto);

        List<PaymentDetailsDto> actual = paymentService.findAllPayments();

        assertEquals(List.of(firstDto, secondDto), actual);
    }

    private PaymentStatus buildStatus(Long id, PaymentStatusName name) {
        PaymentStatus status = new PaymentStatus();
        status.setId(id);
        status.setName(name);

        return status;
    }

    private Payment buildPayment(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(status);
        payment.setBooking(booking);
        payment.setSessionId(SESSION_ID);
        payment.setSessionUrl(SESSION_URL);
        payment.setAmountToPay(BigDecimal.valueOf(AMOUNT_TOTAL));

        return payment;
    }

    private StripeException stripeException() {
        return new ApiException("stripe error", "req_1", "code", 500, null);
    }
}
