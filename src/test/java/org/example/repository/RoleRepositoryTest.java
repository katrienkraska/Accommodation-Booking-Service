package org.example.repository;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.example.model.user.Role;
import org.example.testutil.UserRoleTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    private static final String DELETE_ALL_SQL =
            "delete/delete-all.sql";

    private static final String INSERT_USERS_SQL =
            "database/user/insert-users.sql";

    public static final Role.RoleName ADMIN = Role.RoleName.ROLE_ADMIN;
    public static final Role.RoleName USER = Role.RoleName.ROLE_USER;

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) throws SQLException {
        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_USERS_SQL));
        }
    }

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should return role Admin")
    void findByRoleName_ExistingRoleAdmin_ShouldReturnRole() {

        Role expectedRole = UserRoleTestUtil.getRoleAdmin();

        Role actualRole = roleRepository.findByRoleName(ADMIN)
                .orElseThrow();

        assertThat(actualRole)
                .usingRecursiveComparison()
                .isEqualTo(expectedRole);
    }

    @Test
    @DisplayName("Should return role User")
    void findByRoleName_ExistingRoleUser_ShouldReturnRole() {

        Role expectedRole = UserRoleTestUtil.getRoleUser();

        Role actualRole = roleRepository.findByRoleName(USER)
                .orElseThrow();

        assertThat(actualRole)
                .usingRecursiveComparison()
                .isEqualTo(expectedRole);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(DELETE_ALL_SQL));
        }
    }
}
