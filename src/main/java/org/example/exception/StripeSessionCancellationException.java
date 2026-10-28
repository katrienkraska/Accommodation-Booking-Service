package org.example.exception;

public class StripeSessionCancellationException extends RuntimeException {
    public StripeSessionCancellationException(String message) {
        super(message);
    }
}
