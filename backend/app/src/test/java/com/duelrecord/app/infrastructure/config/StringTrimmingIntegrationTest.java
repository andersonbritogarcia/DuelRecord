package com.duelrecord.app.infrastructure.config;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(StringTrimmingIntegrationTest.TestSecurityConfig.class)
class StringTrimmingIntegrationTest {

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
    }

    @Test
    void shouldTrimWhitespaceInRequestBody() throws Exception {
        String payload = """
                {
                    "countryCode": " BR ",
                    "name": "  Campinas  "
                }
                """;

        mockMvc.perform(post("/api/geo/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Campinas"));

        var created = cityRepository.findAll().get(0);
        assertEquals("Campinas", created.getName());
    }
}
