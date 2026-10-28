package org.example.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.example.model.telegram.TelegramChat;
import org.example.model.user.Role;
import org.example.testutil.TelegramChatTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TelegramChatRepositoryTest {

    private static final String DELETE_ALL_SQL =
            "delete/delete-all.sql";

    private static final String INSERT_TELEGRAM_CHATS_SQL =
            "database/telegram/insert-telegram-chats.sql";

    private static final Role.RoleName USER_ROLE =
            Role.RoleName.ROLE_USER;

    private static final String INSERT_USERS_SQL =
            "database/user/insert-users.sql";

    @Autowired
    private TelegramChatRepository telegramChatRepository;

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) throws SQLException {
        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_USERS_SQL));

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_TELEGRAM_CHATS_SQL));
        }
    }

    @Test
    @DisplayName("Should return TelegramChat when user with given email exists")
    void getTelegramChatByUserEmail_ExistingEmail_ShouldReturnChat() {

        String email = "user@example.com";

        TelegramChat expectedChat = TelegramChatTestUtil.createExpectedTelegramChat();

        Optional<TelegramChat> actualResult = telegramChatRepository
                .getTelegramChatByUserEmail(email);

        assertTrue(actualResult.isPresent());

        TelegramChat actualChat = actualResult.get();

        assertEquals(expectedChat.getId(), actualChat.getId());
        assertEquals(expectedChat.getChatId(), actualChat.getChatId());
        assertFalse(actualChat.isDeleted());
    }

    @Test
    @DisplayName("Should return empty Optional when no TelegramChat found for given email")
    void getTelegramChatByUserEmail_NonExistingEmail_ShouldReturnEmptyOptional() {

        String email = "notfounduser@example.com";

        Optional<TelegramChat> actualResult = telegramChatRepository
                .getTelegramChatByUserEmail(email);

        assertTrue(actualResult.isEmpty());
    }

    @Test
    @DisplayName("Should return TelegramChats when user has the specified role")
    void findAllByUserRolesRole_ExistingRole_ShouldReturnChats() {

        TelegramChat expectedChat = TelegramChatTestUtil.createExpectedTelegramChat();

        List<TelegramChat> actualResult =
                telegramChatRepository.findAllByUser_Roles_Role(USER_ROLE);

        assertFalse(actualResult.isEmpty());

        TelegramChat actualChat = actualResult.get(0);

        assertEquals(expectedChat.getId(), actualChat.getId());
        assertEquals(expectedChat.getChatId(), actualChat.getChatId());
        assertEquals(expectedChat.getChatId(), actualChat.getChatId());

        assertTrue(actualChat
                .getUser()
                .getRoles()
                .stream()
                .anyMatch(role -> role.getRole().equals(USER_ROLE)));
    }

    @AfterEach
    void teardown(@Autowired DataSource dataSource) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(DELETE_ALL_SQL));
        }
    }
}
