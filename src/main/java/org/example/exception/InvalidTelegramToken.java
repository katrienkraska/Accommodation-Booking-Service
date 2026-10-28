package org.example.exception;

public class InvalidTelegramToken extends RuntimeException {
    public InvalidTelegramToken(String message) {
        super(message);
    }
}
