package org.example.scheduler;

import com.stripe.model.checkout.Session;
import com.stripe.exception.StripeException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.exception.StripeSessionCancellationException;
import org.example.model.booking.Booking;
import org.example.model.booking.BookingStatusName;
import org.example.model.payment.Payment;
import org.example.model.payment.PaymentStatus;
import org.example.model.telegram.TelegramChat;
import org.example.model.user.Role;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.PaymentStatusRepository;
import org.example.repository.TelegramChatRepository;
import org.example.telegram.BookingNotificationBot;
import org.example.telegram.TelegramNotificationBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final BookingNotificationBot bookingNotificationBot;
    private final TelegramChatRepository telegramChatRepository;

    @Scheduled(cron = "0 0 10 * * *")
    public void ifCheckInDateIsToday() {
        LocalDate today = LocalDate.now();

        List<Booking> bookingList = bookingRepository
                .findAllByCheckInDate(today, BookingStatusName.CONFIRMED);

        if (bookingList.isEmpty()) {
            return;
        }

        bookingList.forEach(booking -> bookingNotificationBot.sendMessage(
                booking.getUser().getEmail(),
                TelegramNotificationBuilder.generateUpcomingBookingReminder(booking)));
    }

    @Scheduled(cron = "0 00 12 * * * ")
    @Transactional
    public void ifCheckOutDateIsToday() {
        LocalDate today = LocalDate.now();

        List<Booking> bookingList = bookingRepository
                .findByCheckOutDateAndStatus(today, BookingStatusName.CONFIRMED);

        if (bookingList.isEmpty()) {
            getAdminsChatIds().forEach(b ->
                    bookingNotificationBot.sendMessage(b,
                            TelegramNotificationBuilder.getNoExpiredBookingsMessage()));
            return;
        }

        updateBookingsStatus(bookingList, BookingStatusName.EXPIRED);
        bookingList.forEach(booking -> bookingNotificationBot.sendMessage(
                booking.getUser().getEmail(), TelegramNotificationBuilder.bookindExpired(booking)));
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Transactional
    public void ifBookingUnconfirmedOneHour() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1L);

        List<Booking> bookingList = bookingRepository
                .findAllByCreatedAtAndStatus(oneHourAgo, BookingStatusName.PENDING);

        if (bookingList.isEmpty()) {
            return;
        }

        updateBookingsStatus(bookingList, BookingStatusName.CANCELED);
        cancelPayments(bookingList);
        bookingList.forEach(booking -> bookingNotificationBot.sendMessage(
                booking.getUser().getEmail(),
                TelegramNotificationBuilder.bookindExpired(booking)));
    }

    private void updateBookingsStatus(
            List<Booking> bookingList, BookingStatusName statusName) {
        bookingList.forEach(booking -> booking.setStatus(statusName));
        bookingRepository.saveAll(bookingList);
    }

    private void cancelPayments(List<Booking> bookingList) {
        List<Long> bookingIds = bookingList.stream().map(Booking::getId).toList();

        PaymentStatus cancelStatus = paymentStatusRepository
                .findPaymentStatusByName(PaymentStatus.PaymentStatusName.CANCELED);

        List<Payment> paymentList = paymentRepository
                .findAllByBookingIdInAndStatus_Name(bookingIds,
                        PaymentStatus.PaymentStatusName.PENDING);

        for (Payment payment : paymentList) {
            payment.setStatus(cancelStatus);

            try {
                Session sessionById = Session.retrieve(payment.getSessionId());
                sessionById.expire();
            } catch (StripeException e) {
                throw new StripeSessionCancellationException(
                        "Cant cancel stripe session " + e.getMessage());
            }
        }

        paymentRepository.saveAll(paymentList);
    }

    private List<Long> getAdminsChatIds() {
        List<TelegramChat> allByUserRolesName =
                telegramChatRepository.findAllByUser_Roles_Role(Role.RoleName.ROLE_ADMIN);
        return allByUserRolesName.stream().map(TelegramChat::getChatId).toList();
    }
}
