package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.persistence.model.MatchParticipant;

import java.time.Instant;
import java.util.UUID;

public record MatchParticipantResponse(
        UUID id,
        UUID playerId,
        UUID deckIdentityId,
        int seat,
        String displayNameSnapshot,
        boolean isWinner,
        int gameWins,
        Instant createdAt
) {
    public static MatchParticipantResponse fromEntity(MatchParticipant participant) {
        return new MatchParticipantResponse(
                participant.getId(),
                participant.getPlayerId(),
                participant.getDeckIdentityId(),
                participant.getSeat(),
                participant.getDisplayNameSnapshot(),
                participant.isWinner(),
                participant.getGameWins(),
                participant.getCreatedAt()
        );
    }
}
