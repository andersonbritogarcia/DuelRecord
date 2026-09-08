package com.duelrecord.app.match;

import com.duelrecord.app.match.core.model.GameFormat;

import java.time.Instant;
import java.util.UUID;

public record MatchDeletedEvent(
        UUID matchId,
        GameFormat format,
        Instant playedAt,
        Instant occurredAt
) {
    public static MatchDeletedEvent of(UUID matchId, GameFormat format, Instant playedAt) {
        return new MatchDeletedEvent(matchId, format, playedAt, Instant.now());
    }
}
