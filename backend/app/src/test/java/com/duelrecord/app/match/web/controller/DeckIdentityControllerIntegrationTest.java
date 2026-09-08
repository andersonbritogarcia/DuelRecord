package com.duelrecord.app.match.web.controller;

import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import com.duelrecord.app.match.persistence.repository.DeckIdentityCardRepository;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(DeckIdentityControllerIntegrationTest.TestSecurityConfig.class)
class DeckIdentityControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DeckIdentityRepository deckIdentityRepository;

    @Autowired
    private DeckIdentityCardRepository deckIdentityCardRepository;

    @Autowired
    private CardRepository cardRepository;

    private MockMvc mockMvc;

    private Card yoshimaru;
    private Card kraum;
    private Card tasigur;
    private Card kozilek;

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

        deckIdentityCardRepository.deleteAll();
        deckIdentityRepository.deleteAll();
        cardRepository.deleteAll();

        yoshimaru = cardRepository.save(Card.create(
                "scry-yoshi", "ora-yoshi", "Yoshimaru, Ever Faithful", "Legendary Creature — Dog",
                "{W}", BigDecimal.ONE, "W", null, null, null, true, true, false, false));

        kraum = cardRepository.save(Card.create(
                "scry-kraum", "ora-kraum", "Kraum, Ludevic's Opus", "Legendary Creature — Zombie Siren",
                "{3}{U}{R}", BigDecimal.valueOf(5), "UR", null, null, null, true, true, false, false));

        tasigur = cardRepository.save(Card.create(
                "scry-tasi", "ora-tasi", "Tasigur, the Golden Fang", "Legendary Creature — Human Shaman",
                "{5}{B}", BigDecimal.valueOf(6), "UBG", null, null, null, true, false, false, false));

        kozilek = cardRepository.save(Card.create(
                "scry-kozi", "ora-kozi", "Kozilek, the Great Distortion", "Legendary Creature — Eldrazi",
                "{8}{C}{C}", BigDecimal.TEN, "", null, null, null, true, false, false, false));
    }

    @Test
    @DisplayName("Should return 401 when creating deck identity without authentication")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        String payload = """
                {
                    "items": [
                        {
                            "cardId": "%s",
                            "role": "COMMANDER"
                        }
                    ]
                }
                """.formatted(tasigur.getId());

        mockMvc.perform(post("/api/decks/identities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should create single commander deck identity and fetch it publicly by ID")
    void shouldCreateAndFetchDeckIdentity() throws Exception {
        String payload = """
                {
                    "items": [
                        {
                            "cardId": "%s",
                            "role": "COMMANDER"
                        }
                    ]
                }
                """.formatted(tasigur.getId());

        mockMvc.perform(post("/api/decks/identities")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-1").claim("email", "player@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Tasigur, the Golden Fang"))
                .andExpect(jsonPath("$.colorIdentity").value("UBG"))
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].cardId").value(tasigur.getId().toString()))
                .andExpect(jsonPath("$.cards[0].role").value("COMMANDER"));

        UUID createdId = deckIdentityRepository.findAll().get(0).getId();

        // Public GET by ID
        mockMvc.perform(get("/api/decks/identities/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.name").value("Tasigur, the Golden Fang"))
                .andExpect(jsonPath("$.colorIdentity").value("UBG"));
    }

    @Test
    @DisplayName("Should create partner deck identity with combined WUR color identity")
    void shouldCreatePartnerDeckIdentity() throws Exception {
        String payload = """
                {
                    "items": [
                        {
                            "cardId": "%s",
                            "role": "COMMANDER"
                        },
                        {
                            "cardId": "%s",
                            "role": "PARTNER"
                        }
                    ]
                }
                """.formatted(yoshimaru.getId(), kraum.getId());

        mockMvc.perform(post("/api/decks/identities")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-1").claim("email", "player@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Yoshimaru, Ever Faithful / Kraum, Ludevic's Opus"))
                .andExpect(jsonPath("$.colorIdentity").value("WUR"))
                .andExpect(jsonPath("$.cards.length()").value(2));

        assertThat(deckIdentityRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should set 'C' for colorless commander Kozilek")
    void shouldCreateColorlessDeckIdentity() throws Exception {
        String payload = """
                {
                    "items": [
                        {
                            "cardId": "%s",
                            "role": "COMMANDER"
                        }
                    ]
                }
                """.formatted(kozilek.getId());

        mockMvc.perform(post("/api/decks/identities")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-1").claim("email", "player@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Kozilek, the Great Distortion"))
                .andExpect(jsonPath("$.colorIdentity").value("C"))
                .andExpect(jsonPath("$.cards.length()").value(1));
    }

    @Test
    @DisplayName("Should return existing DeckIdentity idempotently when re-submitted")
    void shouldReturnExistingIdempotently() throws Exception {
        String payload = """
                {
                    "items": [
                        {
                            "cardId": "%s",
                            "role": "COMMANDER"
                        }
                    ]
                }
                """.formatted(tasigur.getId());

        mockMvc.perform(post("/api/decks/identities")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-1").claim("email", "player@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        assertThat(deckIdentityRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/api/decks/identities")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-1").claim("email", "player@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        assertThat(deckIdentityRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent deck identity")
    void shouldReturn404ForNonExistent() throws Exception {
        mockMvc.perform(get("/api/decks/identities/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
