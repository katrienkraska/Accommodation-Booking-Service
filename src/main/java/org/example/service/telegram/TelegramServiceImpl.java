package org.example.service.telegram;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserResponseDto;
import org.example.exception.EntityNotFoundException;
import org.example.mapper.UserMapper;
import org.example.model.telegram.TelegramChat;
import org.example.model.user.User;
import org.example.repository.TelegramChatRepository;
import org.example.repository.UserRepository;
import org.example.telegram.EmailTokenService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TelegramServiceImpl implements TelegramService {

    private static final String URL = "https://t.me/QuickBooker_bot?start=";
    private final EmailTokenService emailTokenService;
    private final UserRepository userRepository;
    private final TelegramChatRepository telegramChatRepository;
    private final UserMapper userMapper;

    @Override
    public String getTelegramInviteUrl(String email) {
        String token = emailTokenService.encryptEmail(email);
        return URL + token;
    }

    @Override
    @Transactional
    public UserResponseDto auth(String token, Long chatId) throws Exception {
        String email = emailTokenService.decryptEmail(token);
        Optional<UserResponseDto> optionalUser = ifUserAuthorize(email);
        return optionalUser.orElseGet(() -> createUserAndChat(email, chatId));
    }

    private Optional<UserResponseDto> ifUserAuthorize(String email) {
        Optional<TelegramChat> optionalChat =
                telegramChatRepository.getTelegramChatByUserEmail(email);

        if (optionalChat.isPresent()) {
            User user = optionalChat.get().getUser();
            return Optional.of(userMapper.toDto(user));
        }
        return Optional.empty();
    }

    private UserResponseDto createUserAndChat(String email, Long chatId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cannot find user with email: " + email));

        TelegramChat telegramChat = new TelegramChat();
        telegramChat.setUser(user);
        telegramChat.setChatId(chatId);
        telegramChatRepository.save(telegramChat);
        return userMapper.toDto(user);
    }
}
