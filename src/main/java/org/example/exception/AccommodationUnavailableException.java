package org.example.exception;

public class AccommodationUnavailableException extends RuntimeException {
    public AccommodationUnavailableException(String message) {
        super(message);
    }
}
