package org.example.service.telegram;

import org.example.dto.user.UserResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface TelegramService {
    String getTelegramInviteUrl(String email);

    UserResponseDto auth(String token, Long chatId) throws Exception;
}
