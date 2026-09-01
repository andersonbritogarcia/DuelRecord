package com.duelrecord.app.card.web;

import com.duelrecord.app.card.infrastructure.scryfall.ScryfallClient;
import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(CardControllerIntegrationTest.TestConfig.class)
class CardControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CardRepository cardRepository;

    @MockitoBean
    private ScryfallClient scryfallClient;

    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
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
        cardRepository.deleteAll();
    }

    @Test
    void shouldSearchCommandersFromLocalCache() throws Exception {
        Card tasigur = Card.create(
                "scryfall-tasigur-1",
                "oracle-tasigur-1",
                "Tasigur, the Golden Fang",
                "Legendary Creature — Human Shaman",
                "{5}{B}",
                BigDecimal.valueOf(6.0),
                "UBG",
                "https://cards.scryfall.io/small/tasigur.jpg",
                "https://cards.scryfall.io/normal/tasigur.jpg",
                "https://cards.scryfall.io/art_crop/tasigur.jpg",
                true,
                false,
                false,
                false
        );
        cardRepository.save(tasigur);

        mockMvc.perform(get("/api/cards/commanders")
                        .param("search", "Tasigur")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tasigur, the Golden Fang"))
                .andExpect(jsonPath("$[0].colorIdentity").value("UBG"))
                .andExpect(jsonPath("$[0].isCommanderLegal").value(true));
    }

    @Test
    void shouldGetCardById() throws Exception {
        Card card = Card.create(
                "scryfall-kraum-1",
                "oracle-kraum-1",
                "Kraum, Ludevic's Opus",
                "Legendary Creature — Zombie Horror",
                "{3}{U}{R}",
                BigDecimal.valueOf(5.0),
                "UR",
                "https://small.jpg",
                "https://normal.jpg",
                "https://art.jpg",
                true,
                true,
                false,
                false
        );
        Card saved = cardRepository.save(card);

        mockMvc.perform(get("/api/cards/{id}", saved.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value("Kraum, Ludevic's Opus"))
                .andExpect(jsonPath("$.isPartner").value(true));
    }

    @Test
    void shouldReturn404WhenCardNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/cards/{id}", unknownId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }
}
