package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchSource;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.model.VerificationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecordMatchInput(
        GameFormat format,
        Platform platform,
        MatchStructure matchStructure,
        Instant playedAt,
        Integer round,
        String notes,
        MatchSource source,
        VerificationStatus verificationStatus,
        UUID tournamentParticipationId,
        TournamentParticipationInput tournamentData,
        UUID seat1PlayerId,
        UUID seat1DeckIdentityId,
        UUID seat2PlayerId,
        UUID seat2DeckIdentityId,
        String scorePreset,
        Boolean isIntentionalDraw,
        List<GameInput> games,
        UUID createdByUserId
) {
}
