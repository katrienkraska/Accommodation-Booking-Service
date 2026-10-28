package org.example.exception;

public class BookingPaymentNotAllowedException extends RuntimeException {
    public BookingPaymentNotAllowedException(String message) {
        super(message);
    }
}
