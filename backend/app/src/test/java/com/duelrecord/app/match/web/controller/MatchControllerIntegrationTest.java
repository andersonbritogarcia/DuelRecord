package com.duelrecord.app.match.web.controller;

import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.persistence.repository.UserRepository;
import com.duelrecord.app.match.core.model.DeckCardRole;
import com.duelrecord.app.match.persistence.model.DeckIdentity;
import com.duelrecord.app.match.persistence.model.DeckIdentityCard;
import com.duelrecord.app.match.persistence.repository.DeckIdentityCardRepository;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import com.duelrecord.app.match.persistence.repository.GameRepository;
import com.duelrecord.app.match.persistence.repository.MatchParticipantRepository;
import com.duelrecord.app.match.persistence.repository.MatchRepository;
import com.duelrecord.app.match.persistence.repository.TournamentParticipationRepository;
import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
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
@Import(MatchControllerIntegrationTest.TestSecurityConfig.class)
class MatchControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchParticipantRepository matchParticipantRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TournamentParticipationRepository tournamentParticipationRepository;

    @Autowired
    private DeckIdentityRepository deckIdentityRepository;

    @Autowired
    private DeckIdentityCardRepository deckIdentityCardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private User authUser;
    private Player player1;
    private Player player2;
    private DeckIdentity deck1;
    private DeckIdentity deck2;

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

        gameRepository.deleteAll();
        matchParticipantRepository.deleteAll();
        matchRepository.deleteAll();
        tournamentParticipationRepository.deleteAll();
        deckIdentityCardRepository.deleteAll();
        deckIdentityRepository.deleteAll();
        cardRepository.deleteAll();
        playerRepository.deleteAll();
        userRepository.deleteAll();

        authUser = userRepository.save(new User("anderson@duelrecord.com", "sub-anderson"));
        player1 = playerRepository.save(Player.createRegistered(authUser.getId(), "Anderson", "Anderson BG"));
        player2 = playerRepository.save(Player.createGhost("Opponent Bob", "Bob", "bob_mtgo", null, null));

        Card card1 = cardRepository.save(Card.create(
                "scry-1", "ora-1", "Tasigur, the Golden Fang", "Legendary Creature — Human Shaman",
                "{5}{B}", BigDecimal.valueOf(6), "UBG", null, null, null, true, false, false, false));

        Card card2 = cardRepository.save(Card.create(
                "scry-2", "ora-2", "Yoshimaru, Ever Faithful", "Legendary Creature — Dog",
                "{W}", BigDecimal.ONE, "W", null, null, null, true, true, false, false));

        deck1 = DeckIdentity.create("Tasigur Deck", "UBG", "1111111111111111111111111111111111111111111111111111111111111111");
        deck1.addCard(DeckIdentityCard.create(card1.getId(), DeckCardRole.COMMANDER));
        deck1 = deckIdentityRepository.save(deck1);

        deck2 = DeckIdentity.create("Yoshimaru Deck", "W", "2222222222222222222222222222222222222222222222222222222222222222");
        deck2.addCard(DeckIdentityCard.create(card2.getId(), DeckCardRole.COMMANDER));
        deck2 = deckIdentityRepository.save(deck2);
    }

    @Test
    @DisplayName("Should return 401 when recording match without authentication")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        String payload = """
                {
                    "format": "DUEL_COMMANDER",
                    "platform": "PAPER",
                    "seat1": { "playerId": "%s", "deckIdentityId": "%s" },
                    "seat2": { "playerId": "%s", "deckIdentityId": "%s" },
                    "scorePreset": "2-0"
                }
                """.formatted(player1.getId(), deck1.getId(), player2.getId(), deck2.getId());

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should record 2-0 match preset with 2 games and return 201 Created")
    void shouldRecord2ZeroMatchPreset() throws Exception {
        String payload = """
                {
                    "format": "DUEL_COMMANDER",
                    "platform": "PAPER",
                    "round": 1,
                    "notes": "Fast match",
                    "seat1": { "playerId": "%s", "deckIdentityId": "%s" },
                    "seat2": { "playerId": "%s", "deckIdentityId": "%s" },
                    "scorePreset": "2-0"
                }
                """.formatted(player1.getId(), deck1.getId(), player2.getId(), deck2.getId());

        mockMvc.perform(post("/api/matches")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-anderson").claim("email", "anderson@duelrecord.com").claim("name", "Anderson")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.format").value("DUEL_COMMANDER"))
                .andExpect(jsonPath("$.platform").value("PAPER"))
                .andExpect(jsonPath("$.matchStructure").value("BO3"))
                .andExpect(jsonPath("$.winnerPlayerId").value(player1.getId().toString()))
                .andExpect(jsonPath("$.isDraw").value(false))
                .andExpect(jsonPath("$.participants.length()").value(2))
                .andExpect(jsonPath("$.participants[0].playerId").value(player1.getId().toString()))
                .andExpect(jsonPath("$.participants[0].displayNameSnapshot").value("Anderson BG"))
                .andExpect(jsonPath("$.participants[0].isWinner").value(true))
                .andExpect(jsonPath("$.participants[0].gameWins").value(2))
                .andExpect(jsonPath("$.participants[1].playerId").value(player2.getId().toString()))
                .andExpect(jsonPath("$.participants[1].displayNameSnapshot").value("Bob"))
                .andExpect(jsonPath("$.participants[1].isWinner").value(false))
                .andExpect(jsonPath("$.participants[1].gameWins").value(0))
                .andExpect(jsonPath("$.games.length()").value(2))
                .andExpect(jsonPath("$.games[0].winnerPlayerId").value(player1.getId().toString()))
                .andExpect(jsonPath("$.games[1].winnerPlayerId").value(player1.getId().toString()));

        assertThat(matchRepository.count()).isEqualTo(1);
        assertThat(gameRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should record 0-0 intentional draw with 0 games and draw flag set")
    void shouldRecordIntentionalDraw() throws Exception {
        String payload = """
                {
                    "format": "DUEL_COMMANDER",
                    "platform": "PAPER",
                    "round": 4,
                    "notes": "ID to lock top 8",
                    "seat1": { "playerId": "%s", "deckIdentityId": "%s" },
                    "seat2": { "playerId": "%s", "deckIdentityId": "%s" },
                    "scorePreset": "0-0",
                    "isIntentionalDraw": true
                }
                """.formatted(player1.getId(), deck1.getId(), player2.getId(), deck2.getId());

        mockMvc.perform(post("/api/matches")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-anderson").claim("email", "anderson@duelrecord.com").claim("name", "Anderson")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.winnerPlayerId").isEmpty())
                .andExpect(jsonPath("$.isDraw").value(true))
                .andExpect(jsonPath("$.isIntentionalDraw").value(true))
                .andExpect(jsonPath("$.participants[0].isWinner").value(false))
                .andExpect(jsonPath("$.participants[1].isWinner").value(false))
                .andExpect(jsonPath("$.games.length()").value(0));

        assertThat(gameRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should record match with detailed games and play/draw metadata")
    void shouldRecordDetailedGames() throws Exception {
        String payload = """
                {
                    "format": "DUEL_COMMANDER",
                    "platform": "PAPER",
                    "seat1": { "playerId": "%s", "deckIdentityId": "%s" },
                    "seat2": { "playerId": "%s", "deckIdentityId": "%s" },
                    "games": [
                        {
                            "gameNumber": 1,
                            "winnerPlayerId": "%s",
                            "startingPlayerId": "%s",
                            "isDraw": false,
                            "durationSeconds": 450
                        },
                        {
                            "gameNumber": 2,
                            "winnerPlayerId": "%s",
                            "startingPlayerId": "%s",
                            "isDraw": false,
                            "durationSeconds": 600
                        },
                        {
                            "gameNumber": 3,
                            "winnerPlayerId": "%s",
                            "startingPlayerId": "%s",
                            "isDraw": false,
                            "durationSeconds": 500
                        }
                    ]
                }
                """.formatted(
                player1.getId(), deck1.getId(),
                player2.getId(), deck2.getId(),
                player1.getId(), player1.getId(),
                player2.getId(), player2.getId(),
                player1.getId(), player1.getId()
        );

        mockMvc.perform(post("/api/matches")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-anderson").claim("email", "anderson@duelrecord.com").claim("name", "Anderson")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.winnerPlayerId").value(player1.getId().toString()))
                .andExpect(jsonPath("$.games.length()").value(3))
                .andExpect(jsonPath("$.games[0].gameNumber").value(1))
                .andExpect(jsonPath("$.games[0].durationSeconds").value(450))
                .andExpect(jsonPath("$.games[1].winnerPlayerId").value(player2.getId().toString()));
    }

    @Test
    @DisplayName("Should return 400 when submitting invalid platform for Brawl")
    void shouldRejectInvalidPlatformForBrawl() throws Exception {
        String payload = """
                {
                    "format": "BRAWL",
                    "platform": "PAPER",
                    "seat1": { "playerId": "%s", "deckIdentityId": "%s" },
                    "seat2": { "playerId": "%s", "deckIdentityId": "%s" },
                    "scorePreset": "1-0"
                }
                """.formatted(player1.getId(), deck1.getId(), player2.getId(), deck2.getId());

        mockMvc.perform(post("/api/matches")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-anderson").claim("email", "anderson@duelrecord.com").claim("name", "Anderson")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fetch match publicly by ID with all details")
    void shouldFetchMatchPubliclyById() throws Exception {
        String payload = """
                {
                    "format": "DUEL_COMMANDER",
                    "platform": "PAPER",
                    "seat1": { "playerId": "%s", "deckIdentityId": "%s" },
                    "seat2": { "playerId": "%s", "deckIdentityId": "%s" },
                    "scorePreset": "2-0"
                }
                """.formatted(player1.getId(), deck1.getId(), player2.getId(), deck2.getId());

        String responseBody = mockMvc.perform(post("/api/matches")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-anderson").claim("email", "anderson@duelrecord.com").claim("name", "Anderson")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID matchId = matchRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/matches/" + matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId.toString()))
                .andExpect(jsonPath("$.winnerPlayerId").value(player1.getId().toString()))
                .andExpect(jsonPath("$.participants.length()").value(2))
                .andExpect(jsonPath("$.games.length()").value(2));
    }

    @Test
    @DisplayName("Should filter matches by playerId and format")
    void shouldFilterMatches() throws Exception {
        String payload = """
                {
                    "format": "DUEL_COMMANDER",
                    "platform": "PAPER",
                    "seat1": { "playerId": "%s", "deckIdentityId": "%s" },
                    "seat2": { "playerId": "%s", "deckIdentityId": "%s" },
                    "scorePreset": "2-0"
                }
                """.formatted(player1.getId(), deck1.getId(), player2.getId(), deck2.getId());

        mockMvc.perform(post("/api/matches")
                        .with(jwt().jwt(jwt -> jwt.subject("sub-anderson").claim("email", "anderson@duelrecord.com").claim("name", "Anderson")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/matches")
                        .param("playerId", player1.getId().toString())
                        .param("format", "DUEL_COMMANDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/matches")
                        .param("format", "BRAWL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Should return 404 for non-existent match ID")
    void shouldReturn404ForNonExistentMatch() throws Exception {
        mockMvc.perform(get("/api/matches/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
