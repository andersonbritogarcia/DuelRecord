package com.duelrecord.app.match;

import java.util.UUID;

public record MatchParticipantSnapshot(
        UUID playerId,
        UUID deckIdentityId,
        int seat,
        String displayNameSnapshot,
        boolean isWinner,
        int gameWins
) {
}
