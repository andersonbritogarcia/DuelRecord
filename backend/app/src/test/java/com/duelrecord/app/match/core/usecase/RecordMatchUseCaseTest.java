package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.MatchCreatedEvent;
import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchSource;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.model.VerificationStatus;
import com.duelrecord.app.match.core.validator.FormatRulesValidator;
import com.duelrecord.app.match.persistence.model.Match;
import com.duelrecord.app.match.persistence.model.TournamentParticipation;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import com.duelrecord.app.match.persistence.repository.MatchRepository;
import com.duelrecord.app.match.persistence.repository.TournamentParticipationRepository;
import com.duelrecord.app.player.PlayerApi;
import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.shared.exceptions.BusinessException;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordMatchUseCaseTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private DeckIdentityRepository deckIdentityRepository;

    @Mock
    private TournamentParticipationRepository tournamentParticipationRepository;

    @Mock
    private PlayerApi playerApi;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private FormatRulesValidator formatRulesValidator = new FormatRulesValidator();

    @InjectMocks
    private RecordMatchUseCase recordMatchUseCase;

    @Captor
    private ArgumentCaptor<Match> matchCaptor;

    @Captor
    private ArgumentCaptor<MatchCreatedEvent> eventCaptor;

    private UUID player1Id;
    private UUID player2Id;
    private UUID deck1Id;
    private UUID deck2Id;
    private UUID userId;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        deck1Id = UUID.randomUUID();
        deck2Id = UUID.randomUUID();

        player1 = Player.createRegistered(userId, "Alice", "Alice MTG");
        player2 = Player.createGhost("Bob", "Bob MTG", "bob_mtgo", null, null);
        player1Id = player1.getId();
        player2Id = player2.getId();
    }

    private void mockValidDependencies() {
        when(playerApi.findPlayerById(player1Id)).thenReturn(Optional.of(player1));
        when(playerApi.findPlayerById(player2Id)).thenReturn(Optional.of(player2));
        when(deckIdentityRepository.existsById(deck1Id)).thenReturn(true);
        when(deckIdentityRepository.existsById(deck2Id)).thenReturn(true);
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Should record 2-0 match preset successfully with 2 games generated and publish MatchCreatedEvent")
    void shouldRecord2ZeroMatchPreset() {
        mockValidDependencies();

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                Instant.now(),
                1,
                "Round 1 match",
                MatchSource.MANUAL,
                VerificationStatus.UNVERIFIED,
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

        Match recorded = recordMatchUseCase.execute(input);

        verify(matchRepository).save(matchCaptor.capture());
        Match saved = matchCaptor.getValue();

        assertThat(saved.getFormat()).isEqualTo(GameFormat.DUEL_COMMANDER);
        assertThat(saved.getPlatform()).isEqualTo(Platform.PAPER);
        assertThat(saved.getMatchStructure()).isEqualTo(MatchStructure.BO3);
        assertThat(saved.getWinnerPlayerId()).isEqualTo(player1.getId());
        assertThat(saved.isDraw()).isFalse();
        assertThat(saved.isIntentionalDraw()).isFalse();

        assertThat(saved.getParticipants()).hasSize(2);
        var seat1 = saved.getParticipants().get(0);
        assertThat(seat1.getPlayerId()).isEqualTo(player1.getId());
        assertThat(seat1.getDisplayNameSnapshot()).isEqualTo("Alice MTG");
        assertThat(seat1.isWinner()).isTrue();
        assertThat(seat1.getGameWins()).isEqualTo(2);

        var seat2 = saved.getParticipants().get(1);
        assertThat(seat2.getPlayerId()).isEqualTo(player2.getId());
        assertThat(seat2.getDisplayNameSnapshot()).isEqualTo("Bob MTG");
        assertThat(seat2.isWinner()).isFalse();
        assertThat(seat2.getGameWins()).isEqualTo(0);

        assertThat(saved.getGames()).hasSize(2);
        assertThat(saved.getGames().get(0).getWinnerPlayerId()).isEqualTo(player1.getId());
        assertThat(saved.getGames().get(1).getWinnerPlayerId()).isEqualTo(player1.getId());

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        MatchCreatedEvent event = eventCaptor.getValue();
        assertThat(event.matchId()).isEqualTo(saved.getId());
        assertThat(event.format()).isEqualTo(GameFormat.DUEL_COMMANDER);
        assertThat(event.platform()).isEqualTo(Platform.PAPER);
        assertThat(event.winnerPlayerId()).isEqualTo(player1.getId());
        assertThat(event.isDraw()).isFalse();
        assertThat(event.participants()).hasSize(2);
        assertThat(event.gamesCount()).isEqualTo(2);
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("Should record 2-1 match preset with 3 games generated")
    void shouldRecord2OneMatchPreset() {
        mockValidDependencies();

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER_500,
                Platform.MTGO,
                null,
                null,
                2,
                null,
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                "2-1",
                false,
                null,
                userId
        );

        Match recorded = recordMatchUseCase.execute(input);

        assertThat(recorded.getWinnerPlayerId()).isEqualTo(player1.getId());
        assertThat(recorded.getGames()).hasSize(3);
        assertThat(recorded.getGames().get(0).getWinnerPlayerId()).isEqualTo(player1.getId());
        assertThat(recorded.getGames().get(1).getWinnerPlayerId()).isEqualTo(player2.getId());
        assertThat(recorded.getGames().get(2).getWinnerPlayerId()).isEqualTo(player1.getId());
        verify(eventPublisher).publishEvent(any(MatchCreatedEvent.class));
    }

    @Test
    @DisplayName("Should record 0-2 match preset with player 2 as winner")
    void shouldRecord0TwoMatchPreset() {
        mockValidDependencies();

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                "0-2",
                false,
                null,
                userId
        );

        Match recorded = recordMatchUseCase.execute(input);

        assertThat(recorded.getWinnerPlayerId()).isEqualTo(player2.getId());
        assertThat(recorded.getParticipants().get(0).isWinner()).isFalse();
        assertThat(recorded.getParticipants().get(1).isWinner()).isTrue();
        assertThat(recorded.getGames()).hasSize(2);
        verify(eventPublisher).publishEvent(any(MatchCreatedEvent.class));
    }

    @Test
    @DisplayName("Should record 0-0 match preset as DRAW with 0 games")
    void shouldRecord0ZeroMatchPresetAsDraw() {
        mockValidDependencies();

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                "0-0",
                false,
                null,
                userId
        );

        Match recorded = recordMatchUseCase.execute(input);

        assertThat(recorded.getWinnerPlayerId()).isNull();
        assertThat(recorded.isDraw()).isTrue();
        assertThat(recorded.isIntentionalDraw()).isFalse();
        assertThat(recorded.getGames()).isEmpty();
        assertThat(recorded.getParticipants().get(0).isWinner()).isFalse();
        assertThat(recorded.getParticipants().get(1).isWinner()).isFalse();
        verify(eventPublisher).publishEvent(any(MatchCreatedEvent.class));
    }

    @Test
    @DisplayName("Should record INTENTIONAL_DRAW preset as intentional draw with 0 games")
    void shouldRecordIntentionalDrawPreset() {
        mockValidDependencies();

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                "INTENTIONAL_DRAW",
                true,
                null,
                userId
        );

        Match recorded = recordMatchUseCase.execute(input);

        assertThat(recorded.getWinnerPlayerId()).isNull();
        assertThat(recorded.isDraw()).isTrue();
        assertThat(recorded.isIntentionalDraw()).isTrue();
        assertThat(recorded.getGames()).isEmpty();
        verify(eventPublisher).publishEvent(any(MatchCreatedEvent.class));
    }

    @Test
    @DisplayName("Should record match with detailed games and play/draw data")
    void shouldRecordDetailedGames() {
        mockValidDependencies();

        List<GameInput> games = List.of(
                new GameInput(1, player1.getId(), player1.getId(), false, 600, "Clean win"),
                new GameInput(2, null, player2.getId(), true, 900, "Time ran out in turns")
        );

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                null,
                false,
                games,
                userId
        );

        Match recorded = recordMatchUseCase.execute(input);

        assertThat(recorded.getWinnerPlayerId()).isEqualTo(player1.getId());
        assertThat(recorded.isDraw()).isFalse();
        assertThat(recorded.getGames()).hasSize(2);
        assertThat(recorded.getGames().get(0).getWinnerPlayerId()).isEqualTo(player1.getId());
        assertThat(recorded.getGames().get(1).isDraw()).isTrue();
        assertThat(recorded.getGames().get(1).getWinnerPlayerId()).isNull();
        assertThat(recorded.getGames().get(1).getStartingPlayerId()).isEqualTo(player2.getId());
        verify(eventPublisher).publishEvent(any(MatchCreatedEvent.class));
    }

    @Test
    @DisplayName("Should create and link tournament participation when inline tournamentData is provided")
    void shouldCreateInlineTournamentParticipation() {
        mockValidDependencies();

        TournamentParticipation tp = TournamentParticipation.create(
                player1.getId(), "Sunday Local", 1, "Comic Store", 4, "Top 8 cut", Instant.now());
        when(tournamentParticipationRepository.save(any(TournamentParticipation.class))).thenReturn(tp);

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new TournamentParticipationInput("Sunday Local", 1, "Comic Store", 4, "Top 8 cut", null),
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                "2-0",
                false,
                null,
                userId
        );

        Match recorded = recordMatchUseCase.execute(input);

        assertThat(recorded.getTournamentParticipationId()).isEqualTo(tp.getId());
        verify(tournamentParticipationRepository).save(any(TournamentParticipation.class));
        verify(eventPublisher).publishEvent(any(MatchCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when participants are the same player")
    void shouldThrowWhenSamePlayer() {
        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player1Id,
                deck2Id,
                "2-0",
                false,
                null,
                userId
        );

        assertThatThrownBy(() -> recordMatchUseCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.samePlayer");
    }

    @Test
    @DisplayName("Should throw BusinessException when playedAt is in the future")
    void shouldThrowWhenPlayedAtInFuture() {
        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                Instant.now().plus(2, ChronoUnit.HOURS),
                null,
                null,
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

        assertThatThrownBy(() -> recordMatchUseCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.futureDate");
    }

    @Test
    @DisplayName("Should throw BusinessException when platform is invalid for format")
    void shouldThrowWhenPlatformInvalidForFormat() {
        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.ARENA,
                null,
                null,
                null,
                null,
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

        assertThatThrownBy(() -> recordMatchUseCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.invalidPlatform");
    }

    @Test
    @DisplayName("Should throw BusinessException when game is draw but winner is specified")
    void shouldThrowWhenDrawnGameHasWinner() {
        when(playerApi.findPlayerById(player1Id)).thenReturn(Optional.of(player1));
        when(playerApi.findPlayerById(player2Id)).thenReturn(Optional.of(player2));
        when(deckIdentityRepository.existsById(deck1Id)).thenReturn(true);
        when(deckIdentityRepository.existsById(deck2Id)).thenReturn(true);

        List<GameInput> games = List.of(
                new GameInput(1, player1.getId(), player1.getId(), true, null, null)
        );

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                player1Id,
                deck1Id,
                player2Id,
                deck2Id,
                null,
                false,
                games,
                userId
        );

        assertThatThrownBy(() -> recordMatchUseCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.drawCannotHaveWinner");
    }

    @Test
    @DisplayName("Should throw BusinessException when deck identity is not found")
    void shouldThrowWhenDeckNotFound() {
        when(playerApi.findPlayerById(player1Id)).thenReturn(Optional.of(player1));
        when(playerApi.findPlayerById(player2Id)).thenReturn(Optional.of(player2));
        when(deckIdentityRepository.existsById(deck1Id)).thenReturn(false);

        RecordMatchInput input = new RecordMatchInput(
                GameFormat.DUEL_COMMANDER,
                Platform.PAPER,
                null,
                null,
                null,
                null,
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

        assertThatThrownBy(() -> recordMatchUseCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.deckIdentityNotFound");
    }
}
