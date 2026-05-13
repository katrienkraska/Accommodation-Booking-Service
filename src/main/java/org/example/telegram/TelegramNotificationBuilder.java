package org.example.telegram;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.example.model.accommodation.Address;
import org.example.model.booking.Booking;
import org.example.model.payment.Payment;
import org.springframework.stereotype.Component;

@Component
public class TelegramNotificationBuilder {

    private static final String TOKEN_NOT_VALID_TEMPLATE =
            "Hello! The authentication token you provided is invalid. "
                    + "Please try again or contact support.";

    private static final String BOOKING_STARTED_TEMPLATE =
            "Welcome, %s! You have successfully initiated "
                    + "the booking process.";

    private static final String NO_EXPIRED_BOOKINGS_MESSAGE =
            "Great news! There are no expired bookings today!";

    private static final String BOOKING_EXPIRATION_NOTIFICATION_TEMPLATE =
            "Hello %s, your booking for %s at %s from %s to %s has expired. "
                    + "You can make a new reservation at your convenience.";

    private static final String BOOKING_CREATED_TEMPLATE =
            "Hello %s, your booking for %s located at %s from %s "
                    + "to %s has been successfully created.";

    private static final String BOOKING_UPDATED_TEMPLATE =
            "Hi %s, your booking for %s at %s has been updated. "
                    + "New dates: %s to %s.";

    private static final String BOOKING_CONFIRMED_TEMPLATE =
            "Dear %s, your booking for %s at %s from %s to %s "
                    + "has been confirmed. We look forward to welcoming you!";

    private static final String UPCOMING_BOOKING_REMINDER_TEMPLATE =
            "Hello %s! Just a reminder that your stay at %s, located at %s, "
                    + "begins on %s and ends on %s. Check-in starts at 12:00. See you soon!";

    private static final String BOOKING_CANCELLED_TEMPLATE =
            "Hello %s, your booking for %s at %s from %s to %s "
                    + "has been canceled.";

    private static final String PAYMENT_INITIATED_TEMPLATE =
            "Hello %s, your payment process for booking ID "
                    + "%s has been initiated. To complete the payment, please use the following link: %s";

    private static final String PAYMENT_SUCCESSFUL_TEMPLATE =
            "Congratulations %s! Your payment for booking ID %s has been successfully completed.";

    private static final String PAYMENT_CANCELLED_TEMPLATE =
            "Hello %s, your payment for booking ID %s has been canceled.";

    private static final String DD_MM_YYYY = "dd.MM.yyyy";
    private static final String COMMA = ", ";
    private static final String STREET_ = "Street ";
    private static final String HOUSE_ = "House ";
    private static final String APARTMENT = ", Apartment ";

    public static String dontValidToken() {
        return TOKEN_NOT_VALID_TEMPLATE;
    }

    public static String bookingStarted(String userName) {
        return String.format(BOOKING_STARTED_TEMPLATE, userName);
    }

    public static String bookingStarted(Booking booking) {
        return getString(booking, BOOKING_CREATED_TEMPLATE);
    }

    public static String bookingUpdated(Booking booking) {
        return getString(booking, BOOKING_UPDATED_TEMPLATE);
    }

    public static String bookingConfirmed(Booking booking) {
        return getString(booking, BOOKING_CONFIRMED_TEMPLATE);
    }

    public static String bookingCancelled(Booking booking) {
        return getString(booking, BOOKING_CANCELLED_TEMPLATE);
    }

    private static String getString(Booking booking, String bookingTimeExpiredTemplate) {
        String firstName = booking.getUser().getFirstName();
        String typeName = booking.getAccommodation().getType().getName().toString();

        LocalDate checkInDate = booking.getCheckInDate();
        LocalDate checkOutDate = booking.getCheckOutDate();

        String address = formatAddress(booking.getAccommodation().getAddress());

        return String.format(
                bookingTimeExpiredTemplate, firstName, typeName,
                address, formatDate(checkInDate), formatDate(checkOutDate));
    }

    public static String paymentInitiated(Payment payment) {
        Booking booking = payment.getBooking();
        String firstName = booking.getUser().getFirstName();
        Long bookingId = booking.getId();
        String sessionUrl = payment.getSessionUrl();
        return String.format(PAYMENT_INITIATED_TEMPLATE, firstName, bookingId, sessionUrl);
    }

    public static String paymentSuccessful(Payment payment) {
        Booking booking = payment.getBooking();
        String firstName = booking.getUser().getFirstName();
        Long bookingId = booking.getId();
        return String.format(PAYMENT_SUCCESSFUL_TEMPLATE, firstName, bookingId);
    }

    public static String paymentCancelled(Payment payment) {
        Booking booking = payment.getBooking();
        String firstName = booking.getUser().getFirstName();
        Long bookingId = booking.getId();
        return String.format(PAYMENT_CANCELLED_TEMPLATE, firstName, bookingId);
    }

    public static String bookindExpired(Booking booking) {
        return getString(booking, BOOKING_EXPIRATION_NOTIFICATION_TEMPLATE);
    }

    public static String getNoExpiredBookingsMessage() {
        return NO_EXPIRED_BOOKINGS_MESSAGE;
    }

    public static String generateUpcomingBookingReminder(Booking booking) {
        return getString(booking, UPCOMING_BOOKING_REMINDER_TEMPLATE);
    }

    private static String formatDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(DD_MM_YYYY));
    }

    private static String formatAddress(Address address) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(address.getCountry()).append(COMMA)
                .append(address.getCity()).append(COMMA)
                .append(STREET_).append(address.getStreet()).append(COMMA)
                .append(HOUSE_).append(address.getHouseNumber());

        if (address.getApartmentNumber() != null
                && !address.getApartmentNumber().isEmpty()) {
            stringBuilder.append(APARTMENT).append(address.getApartmentNumber());
        }
        return stringBuilder.toString();
    }
}
