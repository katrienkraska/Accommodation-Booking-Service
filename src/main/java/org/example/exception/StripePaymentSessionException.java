package org.example.exception;

public class StripePaymentSessionException extends RuntimeException {
    public StripePaymentSessionException(String message) {
        super(message);
    }
}
