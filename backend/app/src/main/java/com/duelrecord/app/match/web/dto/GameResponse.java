package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.persistence.model.Game;

import java.time.Instant;
import java.util.UUID;

public record GameResponse(
        UUID id,
        int gameNumber,
        UUID winnerPlayerId,
        UUID startingPlayerId,
        boolean isDraw,
        Integer durationSeconds,
        String notes,
        Instant createdAt
) {
    public static GameResponse fromEntity(Game game) {
        return new GameResponse(
                game.getId(),
                game.getGameNumber(),
                game.getWinnerPlayerId(),
                game.getStartingPlayerId(),
                game.isDraw(),
                game.getDurationSeconds(),
                game.getNotes(),
                game.getCreatedAt()
        );
    }
}
