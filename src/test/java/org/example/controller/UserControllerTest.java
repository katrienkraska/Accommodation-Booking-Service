package org.example.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdatePasswordRequestDto;
import org.example.dto.user.UserUpdateRequestDto;
import org.example.dto.user.UserUpdateRoleRequestDto;
import org.example.model.user.Role;
import org.example.testutil.UserTestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    private static final String DELETE_ALL_SQL =
            "delete/delete-all.sql";

    private static final String INSERT_USERS_SQL =
            "database/user/insert-users.sql";

    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext,
                          @Autowired DataSource dataSource) {

        teardown(dataSource);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) throws SQLException {

        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_USERS_SQL));
        }
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should update current user info")
    void update_ValidRequest_ReturnUpdatedUserDto() throws Exception {

        UserUpdateRequestDto requestDto = UserTestUtil.createUserUpdateRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult mvcResult = mockMvc.perform(patch("/user/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        assertEquals(requestDto.getEmail(), actual.getEmail());
        assertEquals(requestDto.getFirstName(), actual.getFirstName());
        assertEquals(requestDto.getLastName(), actual.getLastName());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void update_InvalidRequest_ReturnBadRequest() throws Exception {

        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();

        requestDto.setEmail("invalid-email");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(patch("/user/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should update password")
    void updatePassword_ValidRequest_ReturnOk() throws Exception {

        UserUpdatePasswordRequestDto passwordRequestDto =
                UserTestUtil.createUserUpdatePasswordRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(passwordRequestDto);

        mockMvc.perform(put("/user/me/password")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should return authenticated user info")
    void getInfo_ValidUser_ReturnUserDto() throws Exception {
        UserResponseDto expected = UserTestUtil.createExpectedUserResponseDto();

        MvcResult mvcResult = mockMvc.perform(get("/user/me"))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());

        assertThat(actual.getRoles()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    @DisplayName("Should update user role")
    void updateRoles_ValidRequest_ReturnUpdatedUserDto() throws Exception {

        Long userId = 2L;

        UserUpdateRoleRequestDto updateRoleRequestDto = UserTestUtil.createUserUpdateRoleRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(updateRoleRequestDto);

        MvcResult mvcResult = mockMvc.perform(patch("/user/{id}/role", userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        assertThat(actual.getRoles()).extracting("name")
                .contains(Role.RoleName.ROLE_ADMIN.name());
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {

        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection, new ClassPathResource(DELETE_ALL_SQL));
        }
    }
}
