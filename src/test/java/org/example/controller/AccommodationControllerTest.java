package org.example.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.example.dto.accommodation.AccommodationDto;
import org.example.dto.accommodation.AccommodationRequestDto;
import org.example.testutil.AccommodationTestUtil;
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
class AccommodationControllerTest {

    private static final String DELETE_ALL_SQL = "delete/delete-all.sql";
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

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(INSERT_ACCOMMODATION_SQL));
        }
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    @DisplayName("Should create a new accommodation unit when "
            + "request is valid and user has ADMIN role")
    void create_ValidRequest_ReturnsCreatedAccommodation() throws Exception {

        AccommodationRequestDto requestDto = AccommodationTestUtil.createAccommodationRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        AccommodationDto expectedDto = AccommodationTestUtil.createExpectedAccommodationDto();

        MvcResult mvcResult = mockMvc.perform(post("/accommodations")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        AccommodationDto actualDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), AccommodationDto.class
        );

        assertThat(actualDto.getId()).isNotNull();

        assertEquals(0, expectedDto.getDailyRate().compareTo(actualDto.getDailyRate()));

        assertThat(actualDto.getAddressDto()).usingRecursiveComparison()
                .isEqualTo(expectedDto.getAddressDto());

        assertThat(actualDto.getAmenities()).usingRecursiveComparison()
                .isEqualTo(expectedDto.getAmenities());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    @DisplayName("Should return bad request when request invalid")
    void create_InvalidRequest_ShouldReturnBadRequest() throws Exception {

        AccommodationRequestDto accommodationRequestDto = new AccommodationRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(accommodationRequestDto);

        mockMvc.perform(post("/accommodations")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return all accommodations")
    void getAll_ShouldReturnAccommodationList() throws Exception {

        MvcResult result = mockMvc.perform(get("/accommodations"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        List<AccommodationDto> actual = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<List<AccommodationDto>>() {
                }
        );
    }

    @Test
    @WithMockUser
    @DisplayName("Should return accommodation by id")
    void getById_ValidId_ShouldReturnAccommodation() throws Exception {

        Long id = 1L;

        AccommodationDto expectedDto = AccommodationTestUtil.createExpectedAccommodationDto();

        MvcResult mvcResult = mockMvc.perform(get("/accommodations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationDto actualDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), AccommodationDto.class);

        assertEquals(expectedDto.getId(), actualDto.getId());

        assertEquals(0, expectedDto.getDailyRate()
                .compareTo(actualDto.getDailyRate()));

        assertThat(actualDto.getAddressDto())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto.getAddressDto());

        assertThat(actualDto.getAmenities())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto.getAmenities());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    @DisplayName("Should update accommodation")
    void update_ValidRequest_ShouldUpdateAccommodation() throws Exception {

        Long id = 1L;

        AccommodationRequestDto requestDto = AccommodationTestUtil
                .createUpdatedAccommodationRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        AccommodationDto expectedDto = AccommodationTestUtil.createUpdatedAccommodationDto();

        MvcResult mvcResult = mockMvc.perform(put("/accommodations/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationDto actualDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), AccommodationDto.class
        );

        assertEquals(expectedDto.getId(), actualDto.getId());

        assertEquals(0, expectedDto.getDailyRate()
                .compareTo(actualDto.getDailyRate()));

        assertThat(actualDto.getAddressDto())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto.getAddressDto());

        assertThat(actualDto.getAmenities())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto.getAmenities());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
    @DisplayName("Should delete accommodation")
    void deleteById_ValidId_ShouldDeleteAccommodation() throws Exception {

        Long id = 1L;

        mockMvc.perform(delete("/accommodations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
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
