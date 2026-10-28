package org.example.service.payment;

import org.example.dto.payment.PaymentDetailsDto;
import org.example.dto.payment.PaymentDto;
import org.example.exception.BookingPaymentNotAllowedException;
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
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final String STRIPE_PAID_STATUS = "paid";
    private static final String CURRENCY = "USD";
    private static final String PRODUCT_NAME = "Housing reservation";
    private static final String CANT_FIND_BOOKING_ID = "Cant find booking id ";
    private static final String CANT_PAID_BOOKING_ID = "Cant paid booking id ";
    private static final String BECAUSE_BOOKING_STATUS = " because booking status ";
    private static final String USER_WITH_EMAIL = "User with email ";
    private static final String CANT_HAVE_PERMISSION_TO_BOOKING_WITH_ID =
            " cant have permission to booking with id. ";
    private static final String FAILED_TO_RETRIEVE_STRIPE_SESSION =
            "Failed to retrieve Stripe session. ";
    private static final String CANT_FIND_PAYMENT_WHERE_SESSION_ID =
            "Cant find payment where session id. ";
    private static final String FAILED_TO_CREATE_STRIPE_CHECKOUT_SESSION =
            "Failed to create Stripe Checkout session. ";
    private static final int VAL = 100;
    private static final String CAN_T_PAY_BOOKING_WITH_ID = "Can't pay booking with id ";
    private static final String CANT_CANCEL_STRIPE_SESSION = "Cant cancel stripe session ";
    private static final String CURRENT_PAYMENT_STATUS_IS = "Current payment status is ";
    private static final String PAYMENT_NOT_FOUND_BY_ID = "Payment not found by id ";

    private final String successUrl;
    private final String cancelUrl;
    private final BookingNotificationBot bookingNotificationBot;
    private final PaymentMapper paymentMapper;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;

    @Override
    public String createPaymentCheckoutSession(Long id, String email) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        CANT_FIND_BOOKING_ID + id));

        if (!booking.getStatus().equals(BookingStatusName.PENDING)) {
            throw new BookingPaymentNotAllowedException(
                    CANT_PAID_BOOKING_ID + booking.getId()
                            + BECAUSE_BOOKING_STATUS + booking.getStatus());
        }

        if (!email.equals(booking.getUser().getEmail())) {
            throw new UnauthorizedBookingAccessException(USER_WITH_EMAIL + email
                    + CANT_HAVE_PERMISSION_TO_BOOKING_WITH_ID + booking.getId());
        }

        Optional<String> paymentUrlOptional = getUrlIfPaymentCreate(id);
        if (paymentUrlOptional.isPresent()) {
            return paymentUrlOptional.get();
        }

        SessionCreateParams sessionParams = getSessionParams(booking);
        Session session = getStripeSession(sessionParams);
        Payment payment = new Payment();
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setBooking(booking);
        payment.setAmountToPay(BigDecimal.valueOf(session.getAmountTotal()));
        setStatusToPayment(payment, PaymentStatusName.PENDING);
        paymentRepository.save(payment);
        String message = TelegramNotificationBuilder.paymentInitiated(payment);
        bookingNotificationBot.sendMessage(email, message);
        return session.getUrl();
    }

    @Override
    public PaymentDto successPayment(String sessionId) {

        Payment payment = paymentRepository.findPaymentBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        PAYMENT_NOT_FOUND_BY_ID + sessionId));

        if (!payment.getStatus().getName().equals(PaymentStatusName.PENDING)) {
            return paymentMapper.toDto(payment);
        }

        Session session = getSessionById(payment.getSessionId());
        if (!STRIPE_PAID_STATUS.equals(session.getPaymentStatus())) {
            throw new PaymentNotConfirmedException(
                    CURRENT_PAYMENT_STATUS_IS + session.getPaymentStatus());
        }

        updateBookingStatus(payment.getBooking(), BookingStatusName.CONFIRMED);
        updatePaymentStatus(payment, PaymentStatusName.PAID);
        String paymentMessage = TelegramNotificationBuilder.paymentSuccessful(payment);
        String bookingMessage = TelegramNotificationBuilder.bookingConfirmed(payment.getBooking());
        String email = payment.getBooking().getUser().getEmail();
        bookingNotificationBot.sendMessage(email, paymentMessage);
        bookingNotificationBot.sendMessage(email, bookingMessage);
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto cancelPaymentAndBooking(String sessionId) {

        Payment payment = getPaymentBySessionId(sessionId);
        checkPaymentStatus(payment);
        updatePaymentStatus(payment, PaymentStatus.PaymentStatusName.CANCELED);
        updateBookingStatus(payment.getBooking(), BookingStatusName.CANCELED);
        Session sessionById = getSessionById(payment.getSessionId());
        try {
            sessionById.expire();
        } catch (StripeException e) {
            throw new StripePaymentSessionException(
                    CANT_CANCEL_STRIPE_SESSION
                            + e.getMessage());
        }
        String paymentMessage = TelegramNotificationBuilder.paymentCancelled(payment);
        String bookingMessage = TelegramNotificationBuilder.bookingCancelled(payment.getBooking());
        String email = payment.getBooking().getUser().getEmail();
        bookingNotificationBot.sendMessage(email, paymentMessage);
        bookingNotificationBot.sendMessage(email, bookingMessage);

        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentDetailsDto> findPaymentsByUserEmail(String email) {
        return paymentRepository.findPaymentByBookingUserEmail(email).stream()
                .map(paymentMapper::toDetailsDto)
                .toList();
    }

    @Override
    public List<PaymentDetailsDto> findAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toDetailsDto)
                .toList();
    }

    private Optional<String> getUrlIfPaymentCreate(Long bookingId) {
        String url = null;
        Optional<Payment> optional = paymentRepository.findPaymentByBookingId(bookingId);

        if (optional.isPresent()) {
            Payment payment = optional.get();

            if (payment.getStatus().getName().equals(
                    PaymentStatus.PaymentStatusName.PENDING)) {
                url = payment.getSessionUrl();
            } else {
                throw new BookingPaymentNotAllowedException(
                        CAN_T_PAY_BOOKING_WITH_ID + bookingId);
            }
        }
        return Optional.ofNullable(url);
    }

    private SessionCreateParams getSessionParams(Booking booking) {
        return new SessionCreateParams.Builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(getLineItem(booking))
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .build();
    }

    private SessionCreateParams.LineItem getLineItem(Booking booking) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(CURRENCY)
                        .setUnitAmount(getAmount(booking))
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(PRODUCT_NAME)
                                .setDescription(bookingMapper.getBookingDescription(booking))
                                .build())
                        .build())
                .setQuantity(getQuantity(booking))
                .build();
    }

    private long getAmount(Booking booking) {
        BigDecimal dailyRate = booking.getAccommodation().getDailyRate();
        return dailyRate.multiply(BigDecimal.valueOf(VAL)).longValue();
    }

    private long getQuantity(Booking booking) {
        LocalDate checkInDate = booking.getCheckInDate();
        LocalDate checkOutDate = booking.getCheckOutDate();
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    private Session getStripeSession(SessionCreateParams sessionCreateParams) {
        try {
            return Session.create(sessionCreateParams);
        } catch (StripeException e) {
            throw new StripePaymentSessionException(
                    FAILED_TO_CREATE_STRIPE_CHECKOUT_SESSION + e.getMessage());
        }
    }

    private void setStatusToPayment(Payment payment, PaymentStatus.PaymentStatusName paymentStatusName) {
        PaymentStatus paidStatus = paymentStatusRepository.findPaymentStatusByName(paymentStatusName);
        payment.setStatus(paidStatus);
    }

    private void updatePaymentStatus(Payment payment, PaymentStatus.PaymentStatusName statusName) {
        setStatusToPayment(payment, statusName);
        paymentRepository.save(payment);
    }

    private Payment getPaymentBySessionId(String sessionId) {
        return paymentRepository.findPaymentBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        CANT_FIND_PAYMENT_WHERE_SESSION_ID + sessionId));
    }

    private void checkPaymentStatus(Payment payment) {
        if (!payment.getStatus().getName()
                .equals(PaymentStatusName.PENDING)) {
            throw new StripePaymentSessionException(
                    CANT_CANCEL_STRIPE_SESSION + payment.getId()
                            + BECAUSE_BOOKING_STATUS + payment.getStatus());
        }
    }

    private Session getSessionById(String id) {
        try {
            return Session.retrieve(id);
        } catch (StripeException e) {
            throw new StripePaymentSessionException(
                    FAILED_TO_RETRIEVE_STRIPE_SESSION
                            + e.getMessage());
        }
    }

    private void updateBookingStatus(Booking booking, BookingStatusName bookingStatusName) {
        booking.setStatus(bookingStatusName);
        bookingRepository.save(booking);
    }
}
