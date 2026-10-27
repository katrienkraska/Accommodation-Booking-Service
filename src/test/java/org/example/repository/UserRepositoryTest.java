package org.example.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import org.example.model.user.User;
import org.example.testutil.UserTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    private static final String DELETE_ALL_SQL = "delete/delete-all.sql";
    private static final String INSERT_USERS_SQL = "database/user/insert-users.sql";

    @Autowired
    private  UserRepository userRepository;

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) throws SQLException {
        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_USERS_SQL));
        }
    }

    @AfterEach
    void teardown(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(DELETE_ALL_SQL));
        }
    }

    @Test
    @DisplayName("Should return user with roles when user with given email exists")
    void findUserByEmail_ExistingEmail_ShouldReturnUserWithRoles() {
        User expectedUser = UserTestUtil.getExpectedUser();
        String email = "user@example.com";

        Optional<User> actualResult = userRepository.findByEmail(email);
        assertTrue(actualResult.isPresent());

        User actualUser = actualResult.get();
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("Should return empty Optional when user with given email does not exist")
    void findUserByEmail_NonExistingEmail_ShouldReturnEmptyOptional() {
        String nonExistingEmail = "unknown@example.com";

        Optional<User> actualResult = userRepository.findByEmail(nonExistingEmail);
        assertTrue(actualResult.isEmpty());
    }

    @Test
    @DisplayName("Should return user with roles when user with given ID exists")
    void findUserById_ExistingId_ShouldReturnUserWithRoles() {
        User expectedUser = UserTestUtil.getExpectedUser();
        Long id = expectedUser.getId();

        Optional<User> actualResult = userRepository.findUserById(id);
        assertTrue(actualResult.isPresent());

        User actualUser = actualResult.get();
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("Should return true when user with given email exists")
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        String email = "user@example.com";

        boolean result = userRepository.existsByEmail(email);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user with given email does not exist")
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        String email = "nonexistent@example.com";

        boolean result = userRepository.existsByEmail(email);

        assertFalse(result);
    }
}
