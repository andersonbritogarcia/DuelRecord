package com.duelrecord.app.match.core.usecase;

import java.time.Instant;

public record TournamentParticipationInput(
        String tournamentName,
        Integer placement,
        String storeName,
        Integer swissRounds,
        String notes,
        Instant playedAt
) {
}
