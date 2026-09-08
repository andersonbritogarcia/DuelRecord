package com.duelrecord.app.match;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.model.VerificationStatus;
import com.duelrecord.app.match.persistence.model.Match;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchUpdatedEvent(
        UUID matchId,
        GameFormat format,
        Platform platform,
        Instant playedAt,
        Instant previousPlayedAt,
        VerificationStatus verificationStatus,
        UUID winnerPlayerId,
        boolean isDraw,
        boolean isIntentionalDraw,
        List<MatchParticipantSnapshot> participants,
        int gamesCount,
        Instant occurredAt
) {
    public static MatchUpdatedEvent from(Match match, Instant previousPlayedAt) {
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

        return new MatchUpdatedEvent(
                match.getId(),
                match.getFormat(),
                match.getPlatform(),
                match.getPlayedAt(),
                previousPlayedAt != null ? previousPlayedAt : match.getPlayedAt(),
                match.getVerificationStatus(),
                match.getWinnerPlayerId(),
                match.isDraw(),
                match.isIntentionalDraw(),
                participantSnapshots,
                gamesCount,
                Instant.now()
        );
    }
}
