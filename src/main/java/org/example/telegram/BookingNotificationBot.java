package org.example.telegram;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserResponseDto;
import org.example.exception.InvalidTelegramToken;
import org.example.exception.TelegramBotException;
import org.example.model.telegram.TelegramChat;
import org.example.repository.TelegramChatRepository;
import org.example.service.telegram.TelegramService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class BookingNotificationBot extends TelegramLongPollingBot {

    private final TelegramService telegramService;
    private final TelegramChatRepository telegramChatRepository;

    @Value("${telegram.bot.username}")
    private String botUserName;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            Long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/start")) {
                String token = getToken(messageText);

                try {
                    UserResponseDto userResponseDto = telegramService.auth(token, chatId);
                    sendMessage(chatId, TelegramNotificationBuilder
                            .bookingStarted(userResponseDto.getFirstName()));
                } catch (Exception e) {
                    sendMessage(chatId, TelegramNotificationBuilder.dontValidToken());
                    throw new InvalidTelegramToken("Cant decode token: " + token);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private String getToken(String message) {
        String[] tokens = message.split(" ");

        if (tokens.length == 2) {
            return tokens[1];
        }
        throw new InvalidTelegramToken("Massage dont have token");
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new TelegramBotException("Telegram bot exception: " + e.getMessage());
        }
    }

    public void sendMessage(String email, String text) {
        Optional<Long> optionalOfChatId = getChatId(email);

        if (optionalOfChatId.isPresent()) {
            Long chatId = optionalOfChatId.get();
            sendMessage(chatId, text);
        }
    }

    private Optional<Long> getChatId(String email) {
        Optional<TelegramChat> optionalOfChat
                = telegramChatRepository.getTelegramChatByUserEmail(email);
        return optionalOfChat.flatMap(
                telegramChat -> telegramChat.getChatId().describeConstable());
    }
}
