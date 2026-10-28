package org.example.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.testutil.BookingTestUtil;
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
class BookingControllerTest {

    private static final String DELETE_ALL_SQL =
            "delete/delete-all.sql";

    private static final String INSERT_BOOKINGS_SQL =
            "database/booking/insert-bookings.sql";

    private static final String INSERT_USERS_SQL =
            "database/user/insert-users.sql";

    private static final String INSERT_ACCOMMODATION_SQL =
            "database/accommodation/insert-accommodation.sql";

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

            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(INSERT_USERS_SQL));

            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(INSERT_ACCOMMODATION_SQL));

            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(INSERT_BOOKINGS_SQL));
        }
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should create booking")
    void createBooking_ValidRequest_ReturnBookingDto() throws Exception {

        CreateBookingRequest request = BookingTestUtil.createBookingRequest();

        String jsonRequest = objectMapper.writeValueAsString(request);

        BookingDto expected = BookingTestUtil.createExpectedBookingDto();

        MvcResult mvcResult = mockMvc.perform(post("/bookings")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                BookingDto.class);

        assertEquals(expected.getAccommodationId(), actual.getAccommodationId());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getCheckInDate(), actual.getCheckInDate());
        assertEquals(expected.getCheckOutDate(), actual.getCheckOutDate());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should return bad request when request invalid")
    void createBooking_InvalidRequest_ReturnBadRequest() throws Exception {

        CreateBookingRequest createBookingRequest = new CreateBookingRequest();

        String jsonRequest = objectMapper.writeValueAsString(createBookingRequest);

        mockMvc.perform(post("/bookings")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should return my bookings")
    void getMyBookings_ReturnBookingList() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/bookings/my"))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingDto> actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(actual).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    @DisplayName("Should return all bookings for admin")
    void getAllBookings_ReturnBookingList() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/bookings/all"))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingDto> actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(actual).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    @DisplayName("Should return bookings by params")
    void getAll_WithParams_ReturnBookingList() throws Exception {

        Long userId = 2L;

        MvcResult mvcResult = mockMvc.perform(get("/bookings")
                        .param("userId", String.valueOf(userId))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingDto> actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(actual).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should return booking by id")
    void getBookingById_ValidId_ReturnBookingDto() throws Exception {

        Long id = 1L;

        BookingDto expected = BookingTestUtil.createExpectedBookingDto();

        MvcResult mvcResult = mockMvc.perform(get("/bookings/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAccommodationId(), actual.getAccommodationId());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    @DisplayName("Should cancel booking")
    void cancelBooking_ValidId_ReturnNoContent() throws Exception {

        Long id = 1L;

        mockMvc.perform(delete("/bookings/{id}", id))
                .andExpect(status().isOk());
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
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
