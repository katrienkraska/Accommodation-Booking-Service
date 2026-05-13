package org.example.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.exception.TelegramBotException;
import org.example.telegram.BookingNotificationBot;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class TelegramBotConnectionConfig {

    public final BookingNotificationBot bookingNotificationBot;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bookingNotificationBot);
        } catch (TelegramApiException e) {
            throw new TelegramBotException("Telegram exception " + e.getMessage());
        }
    }
}
