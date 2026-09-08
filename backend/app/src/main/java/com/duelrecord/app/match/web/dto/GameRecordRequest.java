package com.duelrecord.app.match.web.dto;

import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record GameRecordRequest(
        @Positive(message = "{validation.gameNumber.positive}")
        int gameNumber,
        UUID winnerPlayerId,
        UUID startingPlayerId,
        boolean isDraw,
        Integer durationSeconds,
        String notes
) {
}
