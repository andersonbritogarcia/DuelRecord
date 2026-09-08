package com.duelrecord.app.match;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchSource;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.model.VerificationStatus;
import com.duelrecord.app.match.persistence.model.Match;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchCreatedEvent(
        UUID matchId,
        GameFormat format,
        Platform platform,
        Instant playedAt,
        MatchSource source,
        VerificationStatus verificationStatus,
        UUID winnerPlayerId,
        boolean isDraw,
        boolean isIntentionalDraw,
        UUID tournamentParticipationId,
        UUID createdBy,
        List<MatchParticipantSnapshot> participants,
        int gamesCount,
        Instant occurredAt
) {
    public static MatchCreatedEvent from(Match match) {
        List<MatchParticipantSnapshot> participantSnapshots = match.getParticipants() == null
                ? List.of()
                : match.getParticipants().stream()
                .map(p -> new MatchParticipantSnapshot(
                        p.getPlayerId(),
                        p.getDeckIdentityId(),
                        p.getSeat(),
                        p.getDisplayNameSnapshot(),
                        p.isWinner(),
                        p.getGameWins()
                ))
                .toList();

        int gamesCount = match.getGames() != null ? match.getGames().size() : 0;

        return new MatchCreatedEvent(
                match.getId(),
                match.getFormat(),
                match.getPlatform(),
                match.getPlayedAt(),
                match.getSource(),
                match.getVerificationStatus(),
                match.getWinnerPlayerId(),
                match.isDraw(),
                match.isIntentionalDraw(),
                match.getTournamentParticipationId(),
                match.getCreatedBy(),
                participantSnapshots,
                gamesCount,
                Instant.now()
        );
    }
}
