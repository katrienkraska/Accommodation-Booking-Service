package org.example.testutil;

import org.example.model.telegram.TelegramChat;

public class TelegramChatTestUtil {

    public static TelegramChat createExpectedTelegramChat() {

        TelegramChat telegramChat = new TelegramChat();
        telegramChat.setId(1L);
        telegramChat.setChatId(123456789L);

        return telegramChat;
    }
}
