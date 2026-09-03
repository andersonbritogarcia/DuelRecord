package com.duelrecord.app.player.web.controller;

import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import com.duelrecord.app.geo.persistence.repository.CountryRepository;
import com.duelrecord.app.player.persistence.model.Player;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(PlayerControllerIntegrationTest.TestSecurityConfig.class)
class PlayerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

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
        cityRepository.deleteAll();
        countryRepository.deleteAll();

        countryRepository.save(Country.create("BR", "Brazil"));
    }

    @Test
    void shouldReturn401WhenCreatingGhostPlayerWithoutToken() throws Exception {
        String payload = """
                {
                    "name": "Ghost Player",
                    "displayName": "Ghost"
                }
                """;

        mockMvc.perform(post("/api/players/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateGhostPlayerWhenAuthenticated() throws Exception {
        String payload = """
                {
                    "name": "Ghost Opponent",
                    "displayName": "Ghost",
                    "mtgoUsername": "ghost_mtgo",
                    "arenaUsername": "ghost_arena"
                }
                """;

        mockMvc.perform(post("/api/players/ghost")
                        .with(jwt().jwt(jwt -> jwt.subject("google-sub-1").claim("email", "player@duelrecord.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Ghost Opponent"))
                .andExpect(jsonPath("$.displayName").value("Ghost"))
                .andExpect(jsonPath("$.mtgoUsername").value("ghost_mtgo"))
                .andExpect(jsonPath("$.arenaUsername").value("ghost_arena"))
                .andExpect(jsonPath("$.isGhost").value(true))
                .andExpect(jsonPath("$.userId").doesNotExist());

        assertEquals(1, playerRepository.count());
        var created = playerRepository.findAll().get(0);
        assertTrue(created.isGhost());
        assertNull(created.getUserId());
    }

    @Test
    void shouldSearchAndGetPlayerById() throws Exception {
        var registered = playerRepository.save(Player.createRegistered(UUID.randomUUID(), "Tasigur Master", "Tasigur"));
        var ghost = playerRepository.save(Player.createGhost("Yoshimaru Player", "Yoshi", null, null, null));

        // Search "Tasi"
        mockMvc.perform(get("/api/players").param("q", "Tasi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Tasigur Master"))
                .andExpect(jsonPath("$.content[0].isGhost").value(false));

        // Search "Yoshi"
        mockMvc.perform(get("/api/players").param("q", "Yoshi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Yoshimaru Player"))
                .andExpect(jsonPath("$.content[0].isGhost").value(true));

        // Get by ID
        mockMvc.perform(get("/api/players/" + registered.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tasigur Master"));

        // Non existent ID returns 404
        mockMvc.perform(get("/api/players/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
