package com.duelrecord.app.match.core.usecase;

import java.util.UUID;

public record GameInput(
        int gameNumber,
        UUID winnerPlayerId,
        UUID startingPlayerId,
        boolean isDraw,
        Integer durationSeconds,
        String notes
) {
}
