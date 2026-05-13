package org.example.exception;

public class TelegramBotException extends RuntimeException {
    public TelegramBotException(String message) {
        super(message);
    }
}
