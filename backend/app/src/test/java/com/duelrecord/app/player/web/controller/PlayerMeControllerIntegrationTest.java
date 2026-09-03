package com.duelrecord.app.player.web.controller;

import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import com.duelrecord.app.geo.persistence.repository.CountryRepository;
import com.duelrecord.app.identity.persistence.repository.UserRepository;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(PlayerMeControllerIntegrationTest.TestSecurityConfig.class)
class PlayerMeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

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

        playerRepository.deleteAll();
        userRepository.deleteAll();
        cityRepository.deleteAll();
        countryRepository.deleteAll();

        countryRepository.save(Country.create("BR", "Brazil"));
    }

    @Test
    void shouldReturn401WhenAccessingMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/players/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAutoProvisionAndReturnPlayerProfileOnGetMe() throws Exception {
        var sub = "google-sub-auto-player";
        var email = "anderson@duelrecord.com";

        mockMvc.perform(get("/api/players/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(sub)
                                .claim("email", email)
                                .claim("name", "Anderson Brito")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Anderson Brito"))
                .andExpect(jsonPath("$.displayName").value("Anderson Brito"))
                .andExpect(jsonPath("$.isGhost").value(false))
                .andExpect(jsonPath("$.userId").isNotEmpty());

        assertEquals(1, playerRepository.count());
        var player = playerRepository.findAll().get(0);
        assertFalse(player.isGhost());
        assertNotNull(player.getUserId());
    }

    @Test
    void shouldUpdatePlayerProfileWithDynamicCity() throws Exception {
        var sub = "google-sub-update-profile";
        var email = "pilot@duelrecord.com";

        // Initial call to auto-provision
        mockMvc.perform(get("/api/players/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(sub)
                                .claim("email", email)
                                .claim("name", "Test Pilot")
                        )))
                .andExpect(status().isOk());

        String updatePayload = """
                {
                    "displayName": "ProPilot_99",
                    "mtgoUsername": "pilot_mtgo",
                    "arenaUsername": "pilot_arena",
                    "newCountryCode": "BR",
                    "newCityName": "Campinas"
                }
                """;

        mockMvc.perform(put("/api/players/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(sub)
                                .claim("email", email)
                                .claim("name", "Test Pilot")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("ProPilot_99"))
                .andExpect(jsonPath("$.mtgoUsername").value("pilot_mtgo"))
                .andExpect(jsonPath("$.arenaUsername").value("pilot_arena"))
                .andExpect(jsonPath("$.city.name").value("Campinas"))
                .andExpect(jsonPath("$.city.country.code").value("BR"));

        var updated = playerRepository.findAll().get(0);
        assertEquals("ProPilot_99", updated.getDisplayName());
        assertEquals("pilot_mtgo", updated.getMtgoUsername());
        assertNotNull(updated.getCity());
        assertEquals("Campinas", updated.getCity().getName());
    }
}
