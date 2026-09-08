package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.MatchCreatedEvent;
import com.duelrecord.app.match.core.model.MatchScorePreset;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.validator.FormatRulesValidator;
import com.duelrecord.app.match.persistence.model.Game;
import com.duelrecord.app.match.persistence.model.Match;
import com.duelrecord.app.match.persistence.model.MatchParticipant;
import com.duelrecord.app.match.persistence.model.TournamentParticipation;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import com.duelrecord.app.match.persistence.repository.MatchRepository;
import com.duelrecord.app.match.persistence.repository.TournamentParticipationRepository;
import com.duelrecord.app.player.PlayerApi;
import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.shared.exceptions.BusinessException;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordMatchUseCase implements UseCase<RecordMatchInput, Match> {

    private final MatchRepository matchRepository;
    private final DeckIdentityRepository deckIdentityRepository;
    private final TournamentParticipationRepository tournamentParticipationRepository;
    private final PlayerApi playerApi;
    private final FormatRulesValidator formatRulesValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Match execute(RecordMatchInput input) {
        validateInputs(input);

        formatRulesValidator.validatePlatform(input.format(), input.platform());
        MatchStructure structure = formatRulesValidator.resolveMatchStructure(input.format(), input.matchStructure());

        Player p1 = playerApi.findPlayerById(input.seat1PlayerId())
                .orElseThrow(() -> new BusinessException("problem.match.participantNotFound", input.seat1PlayerId()));
        Player p2 = playerApi.findPlayerById(input.seat2PlayerId())
                .orElseThrow(() -> new BusinessException("problem.match.participantNotFound", input.seat2PlayerId()));

        if (!deckIdentityRepository.existsById(input.seat1DeckIdentityId())) {
            throw new BusinessException("problem.match.deckIdentityNotFound", input.seat1DeckIdentityId());
        }
        if (!deckIdentityRepository.existsById(input.seat2DeckIdentityId())) {
            throw new BusinessException("problem.match.deckIdentityNotFound", input.seat2DeckIdentityId());
        }

        UUID tournamentParticipationId = resolveTournamentParticipation(input, p1.getId());

        MatchEvaluation evaluation = evaluateScoreAndGames(input, p1.getId(), p2.getId());

        Instant playedAt = input.playedAt() != null ? input.playedAt() : Instant.now();

        Match match = Match.create(
                input.format(),
                tournamentParticipationId,
                playedAt,
                input.platform(),
                structure,
                input.round(),
                input.notes(),
                input.source(),
                input.verificationStatus(),
                evaluation.winnerPlayerId(),
                evaluation.isDraw(),
                evaluation.isIntentionalDraw(),
                input.createdByUserId()
        );

        MatchParticipant participant1 = MatchParticipant.create(
                p1.getId(),
                input.seat1DeckIdentityId(),
                1,
                p1.getDisplayName(),
                p1.getId().equals(evaluation.winnerPlayerId()),
                evaluation.seat1Wins()
        );

        MatchParticipant participant2 = MatchParticipant.create(
                p2.getId(),
                input.seat2DeckIdentityId(),
                2,
                p2.getDisplayName(),
                p2.getId().equals(evaluation.winnerPlayerId()),
                evaluation.seat2Wins()
        );

        match.addParticipant(participant1);
        match.addParticipant(participant2);

        evaluation.games().forEach(match::addGame);

        Match savedMatch = matchRepository.save(match);
        eventPublisher.publishEvent(MatchCreatedEvent.from(savedMatch));
        return savedMatch;
    }

    private void validateInputs(RecordMatchInput input) {
        if (input.format() == null) {
            throw new BusinessException("problem.invalidParameter.detail", "format");
        }
        if (input.platform() == null) {
            throw new BusinessException("problem.invalidParameter.detail", "platform");
        }
        if (input.seat1PlayerId() == null || input.seat2PlayerId() == null) {
            throw new BusinessException("problem.match.invalidParticipantCount");
        }
        if (input.seat1PlayerId().equals(input.seat2PlayerId())) {
            throw new BusinessException("problem.match.samePlayer");
        }
        if (input.seat1DeckIdentityId() == null || input.seat2DeckIdentityId() == null) {
            throw new BusinessException("problem.invalidParameter.detail", "deckIdentityId");
        }
        if (input.createdByUserId() == null) {
            throw new BusinessException("problem.invalidUserId.detail");
        }
        if (input.playedAt() != null && input.playedAt().isAfter(Instant.now().plus(Duration.ofMinutes(15)))) {
            throw new BusinessException("problem.match.futureDate");
        }
    }

    private UUID resolveTournamentParticipation(RecordMatchInput input, UUID player1Id) {
        if (input.tournamentParticipationId() != null) {
            if (!tournamentParticipationRepository.existsById(input.tournamentParticipationId())) {
                throw new EntityNotFoundException("problem.tournamentParticipation.notFound", input.tournamentParticipationId());
            }
            return input.tournamentParticipationId();
        }

        if (input.tournamentData() != null) {
            TournamentParticipationInput data = input.tournamentData();
            TournamentParticipation tp = TournamentParticipation.create(
                    player1Id,
                    data.tournamentName(),
                    data.placement(),
                    data.storeName(),
                    data.swissRounds(),
                    data.notes(),
                    data.playedAt() != null ? data.playedAt() : input.playedAt()
            );
            return tournamentParticipationRepository.save(tp).getId();
        }

        return null;
    }

    private MatchEvaluation evaluateScoreAndGames(RecordMatchInput input, UUID p1Id, UUID p2Id) {
        if (input.scorePreset() != null && !input.scorePreset().isBlank()) {
            MatchScorePreset preset = MatchScorePreset.fromCode(input.scorePreset())
                    .orElseThrow(() -> new BusinessException("problem.match.invalidScorePreset", input.scorePreset()));

            List<Game> generatedGames = generatePresetGames(preset, p1Id, p2Id);
            UUID winnerId = null;
            if (preset.getSeat1Wins() > preset.getSeat2Wins()) {
                winnerId = p1Id;
            } else if (preset.getSeat2Wins() > preset.getSeat1Wins()) {
                winnerId = p2Id;
            }

            boolean isIntentionalDraw = preset.isIntentionalDraw() || Boolean.TRUE.equals(input.isIntentionalDraw());
            return new MatchEvaluation(winnerId, preset.isDraw(), isIntentionalDraw, preset.getSeat1Wins(), preset.getSeat2Wins(), generatedGames);
        }

        if (input.games() != null && !input.games().isEmpty()) {
            List<Game> domainGames = new ArrayList<>();
            int p1Wins = 0;
            int p2Wins = 0;

            for (GameInput g : input.games()) {
                if (g.isDraw()) {
                    if (g.winnerPlayerId() != null) {
                        throw new BusinessException("problem.match.drawCannotHaveWinner");
                    }
                } else {
                    if (g.winnerPlayerId() == null || (!g.winnerPlayerId().equals(p1Id) && !g.winnerPlayerId().equals(p2Id))) {
                        throw new BusinessException("problem.match.invalidGameWinner");
                    }
                    if (g.winnerPlayerId().equals(p1Id)) {
                        p1Wins++;
                    } else {
                        p2Wins++;
                    }
                }

                if (g.startingPlayerId() != null && !g.startingPlayerId().equals(p1Id) && !g.startingPlayerId().equals(p2Id)) {
                    throw new BusinessException("problem.match.invalidStartingPlayer");
                }

                domainGames.add(Game.create(
                        g.gameNumber(),
                        g.winnerPlayerId(),
                        g.startingPlayerId(),
                        g.isDraw(),
                        g.durationSeconds(),
                        g.notes()
                ));
            }

            UUID winnerId = null;
            boolean isDraw;
            if (p1Wins > p2Wins) {
                winnerId = p1Id;
                isDraw = false;
            } else if (p2Wins > p1Wins) {
                winnerId = p2Id;
                isDraw = false;
            } else {
                isDraw = true;
            }

            boolean isIntentionalDraw = Boolean.TRUE.equals(input.isIntentionalDraw());
            return new MatchEvaluation(winnerId, isDraw, isIntentionalDraw, p1Wins, p2Wins, domainGames);
        }

        if (Boolean.TRUE.equals(input.isIntentionalDraw())) {
            return new MatchEvaluation(null, true, true, 0, 0, List.of());
        }

        throw new BusinessException("problem.match.missingGamesOrScore");
    }

    private List<Game> generatePresetGames(MatchScorePreset preset, UUID p1Id, UUID p2Id) {
        List<Game> games = new ArrayList<>();
        switch (preset) {
            case TWO_ZERO -> {
                games.add(Game.create(1, p1Id, p1Id, false, null, null));
                games.add(Game.create(2, p1Id, p2Id, false, null, null));
            }
            case TWO_ONE -> {
                games.add(Game.create(1, p1Id, p1Id, false, null, null));
                games.add(Game.create(2, p2Id, p2Id, false, null, null));
                games.add(Game.create(3, p1Id, p1Id, false, null, null));
            }
            case ONE_TWO -> {
                games.add(Game.create(1, p2Id, p1Id, false, null, null));
                games.add(Game.create(2, p1Id, p2Id, false, null, null));
                games.add(Game.create(3, p2Id, p1Id, false, null, null));
            }
            case ZERO_TWO -> {
                games.add(Game.create(1, p2Id, p1Id, false, null, null));
                games.add(Game.create(2, p2Id, p2Id, false, null, null));
            }
            case ONE_ZERO -> games.add(Game.create(1, p1Id, p1Id, false, null, null));
            case ZERO_ONE -> games.add(Game.create(1, p2Id, p1Id, false, null, null));
            case ONE_ONE -> {
                games.add(Game.create(1, p1Id, p1Id, false, null, null));
                games.add(Game.create(2, p2Id, p2Id, false, null, null));
            }
            case ZERO_ZERO, INTENTIONAL_DRAW -> {
                // 0 games played
            }
        }
        return games;
    }

    private record MatchEvaluation(
            UUID winnerPlayerId,
            boolean isDraw,
            boolean isIntentionalDraw,
            int seat1Wins,
            int seat2Wins,
            List<Game> games
    ) {
    }
}
