package com.duelrecord.app.match;

import com.duelrecord.app.card.CardApi;
import com.duelrecord.app.identity.IdentityApi;
import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.usecase.RecordMatchInput;
import com.duelrecord.app.match.core.usecase.RecordMatchUseCase;
import com.duelrecord.app.match.persistence.model.DeckIdentity;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import com.duelrecord.app.match.persistence.repository.GameRepository;
import com.duelrecord.app.match.persistence.repository.MatchParticipantRepository;
import com.duelrecord.app.match.persistence.repository.MatchRepository;
import com.duelrecord.app.player.PlayerApi;
import com.duelrecord.app.player.persistence.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@ActiveProfiles("test")
@Import(MatchModuleEventsTest.TestEventListener.class)
class MatchModuleEventsTest {

    @Autowired
    private RecordMatchUseCase recordMatchUseCase;

    @Autowired
    private DeckIdentityRepository deckIdentityRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchParticipantRepository matchParticipantRepository;

    @Autowired
    private GameRepository gameRepository;

    @MockitoBean
    private PlayerApi playerApi;

    @MockitoBean
    private CardApi cardApi;

    @MockitoBean
    private IdentityApi identityApi;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private TestEventListener testEventListener;

    @TestComponent
    static class TestEventListener {
        private final BlockingQueue<MatchCreatedEvent> receivedEvents = new LinkedBlockingQueue<>();

        @ApplicationModuleListener
        void onMatchCreated(MatchCreatedEvent event) {
            receivedEvents.offer(event);
        }

        public MatchCreatedEvent poll(long timeout, TimeUnit unit) throws InterruptedException {
            return receivedEvents.poll(timeout, unit);
        }

        public void clear() {
            receivedEvents.clear();
        }
    }

    private UUID player1Id;
    private UUID player2Id;
    private UUID deck1Id;
    private UUID deck2Id;

    @BeforeEach
    void setUp() {
        testEventListener.clear();
        gameRepository.deleteAll();
        matchParticipantRepository.deleteAll();
        matchRepository.deleteAll();
        deckIdentityRepository.deleteAll();

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        Player p1 = Player.createRegistered(UUID.randomUUID(), "Alice", "Alice Display");
        Player p2 = Player.createGhost("Bob", "Bob Display", "bob_mtgo", null, null);

        when(playerApi.findPlayerById(player1Id)).thenReturn(Optional.of(p1));
        when(playerApi.findPlayerById(player2Id)).thenReturn(Optional.of(p2));

        DeckIdentity d1 = deckIdentityRepository.save(
                DeckIdentity.create("Deck 1", "WUB", "1111111111111111111111111111111111111111111111111111111111111111"));
        DeckIdentity d2 = deckIdentityRepository.save(
                DeckIdentity.create("Deck 2", "RG", "2222222222222222222222222222222222222222222222222222222222222222"));
        deck1Id = d1.getId();
        deck2Id = d2.getId();
    }

    @Test
    @DisplayName("Should publish MatchCreatedEvent when match is recorded and receive in asynchronous ApplicationModuleListener")
    void shouldPublishMatchCreatedEventAndReceiveInAsyncListener(Scenario scenario) throws Exception {
        UUID userId = UUID.randomUUID();
        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                Instant.now(),
                1,
                "Event test round 1",
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                "2-0",
                false,
                null,
                userId
        );

        scenario.stimulate(() -> recordMatchUseCase.execute(input))
                .andWaitForEventOfType(MatchCreatedEvent.class)
                .toArriveAndVerify(event -> {
                    assertThat(event.matchId()).isNotNull();
                    assertThat(event.format()).isEqualTo(GameFormat.DUEL_COMMANDER);
                    assertThat(event.platform()).isEqualTo(Platform.PAPER);
                    assertThat(event.winnerPlayerId()).isNotNull();
                    assertThat(event.isDraw()).isFalse();
                    assertThat(event.participants()).hasSize(2);
                    assertThat(event.gamesCount()).isEqualTo(2);
                    assertThat(event.occurredAt()).isNotNull();
                });

        MatchCreatedEvent received = testEventListener.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.format()).isEqualTo(GameFormat.DUEL_COMMANDER);
        assertThat(received.gamesCount()).isEqualTo(2);
        assertThat(received.participants()).hasSize(2);
    }
}
