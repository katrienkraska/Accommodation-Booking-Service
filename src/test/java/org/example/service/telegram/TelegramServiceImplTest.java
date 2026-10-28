package org.example.service.telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.example.dto.user.UserResponseDto;
import org.example.exception.EntityNotFoundException;
import org.example.mapper.UserMapper;
import org.example.model.telegram.TelegramChat;
import org.example.model.user.User;
import org.example.repository.TelegramChatRepository;
import org.example.repository.UserRepository;
import org.example.telegram.EmailTokenService;
import org.example.testutil.UserTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramServiceImplTest {

    private static final String EMAIL = "test@gmail.com";
    private static final String TOKEN = "encryptedToken";
    private static final Long CHAT_ID = 123456789L;

    @Mock
    private EmailTokenService emailTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private TelegramServiceImpl telegramService;

    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = UserTestUtil.getUser();
        userResponseDto = UserTestUtil.createExpectedUserResponseDto();
    }

    @Test
    @DisplayName("getTelegramInviteUrl: returns bot url with encrypted email token")
    void getTelegramInviteUrl_ValidEmail_ShouldReturnUrlWithToken() {
        when(emailTokenService.encryptEmail(EMAIL)).thenReturn(TOKEN);

        String actual = telegramService.getTelegramInviteUrl(EMAIL);

        assertEquals("https://t.me/QuickBooker_bot?start=" + TOKEN, actual);
    }

    @Test
    @DisplayName("auth: chat already exists -> returns user dto without creating new chat")
    void auth_ChatAlreadyExists_ShouldReturnUserDto() throws Exception {
        TelegramChat existingChat = new TelegramChat();
        existingChat.setUser(user);
        existingChat.setChatId(CHAT_ID);

        when(emailTokenService.decryptEmail(TOKEN)).thenReturn(EMAIL);
        when(telegramChatRepository.getTelegramChatByUserEmail(EMAIL))
                .thenReturn(Optional.of(existingChat));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto actual = telegramService.auth(TOKEN, CHAT_ID);

        assertSame(userResponseDto, actual);
        verify(telegramChatRepository, never()).save(any(TelegramChat.class));
        verify(userRepository, never()).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("auth: no chat yet -> creates and saves chat for the user")
    void auth_NewUser_ShouldCreateChatAndReturnUserDto() throws Exception {
        when(emailTokenService.decryptEmail(TOKEN)).thenReturn(EMAIL);
        when(telegramChatRepository.getTelegramChatByUserEmail(EMAIL))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto actual = telegramService.auth(TOKEN, CHAT_ID);

        assertSame(userResponseDto, actual);

        ArgumentCaptor<TelegramChat> captor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(telegramChatRepository).save(captor.capture());
        TelegramChat saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(CHAT_ID, saved.getChatId());
    }

    @Test
    @DisplayName("auth: user not found -> EntityNotFoundException")
    void auth_UserNotFound_ShouldThrowException() throws Exception {
        when(emailTokenService.decryptEmail(TOKEN)).thenReturn(EMAIL);
        when(telegramChatRepository.getTelegramChatByUserEmail(EMAIL))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> telegramService.auth(TOKEN, CHAT_ID));

        assertEquals("Cannot find user with email: " + EMAIL, exception.getMessage());
        verify(telegramChatRepository, never()).save(any(TelegramChat.class));
    }

    @Test
    @DisplayName("auth: token cannot be decrypted -> exception is propagated")
    void auth_InvalidToken_ShouldPropagateException() throws Exception {
        when(emailTokenService.decryptEmail("badToken"))
                .thenThrow(new IllegalArgumentException("Illegal base64 character"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> telegramService.auth("badToken", CHAT_ID));

        assertEquals("Illegal base64 character", exception.getMessage());
        verify(telegramChatRepository, never()).save(any(TelegramChat.class));
    }
}
