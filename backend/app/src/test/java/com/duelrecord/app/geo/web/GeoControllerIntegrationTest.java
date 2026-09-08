package com.duelrecord.app.geo.web;

import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import com.duelrecord.app.geo.persistence.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(GeoControllerIntegrationTest.TestSecurityConfig.class)
class GeoControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    private MockMvc mockMvc;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cityRepository.deleteAll();
        countryRepository.deleteAll();

        countryRepository.save(Country.create("BR", "Brazil"));
        countryRepository.save(Country.create("US", "United States"));
        countryRepository.save(Country.create("FR", "France"));
    }

    @Test
    void shouldListCountries() throws Exception {
        mockMvc.perform(get("/api/geo/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("BR"))
                .andExpect(jsonPath("$[0].name").value("Brazil"));
    }

    @Test
    void shouldRejectAnonymousCityCreation() throws Exception {
        mockMvc.perform(post("/api/geo/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"countryCode\":\"BR\",\"name\":\"Campinas\"}"))
                .andExpect(status().isUnauthorized());
        assertEquals(0, cityRepository.count());
    }

    @Test
    void shouldCreateAndSearchCities() throws Exception {
        String payload = """
                {
                    "countryCode": "BR",
                    "name": "São José dos Campos"
                }
                """;

        mockMvc.perform(post("/api/geo/cities").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("São José dos Campos"))
                .andExpect(jsonPath("$.country.code").value("BR"));

        assertEquals(1, cityRepository.count());

        // Idempotent creation returns existing
        mockMvc.perform(post("/api/geo/cities").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("São José dos Campos"));

        assertEquals(1, cityRepository.count());

        // Search city
        mockMvc.perform(get("/api/geo/cities")
                        .param("country", "BR")
                        .param("q", "José"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("São José dos Campos"));
    }

    @Test
    void shouldReturn404WhenCreatingCityWithNonExistentCountry() throws Exception {
        String payload = """
                {
                    "countryCode": "XX",
                    "name": "Unknown City"
                }
                """;

        mockMvc.perform(post("/api/geo/cities").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Country with code 'XX' was not found"));
    }
}
