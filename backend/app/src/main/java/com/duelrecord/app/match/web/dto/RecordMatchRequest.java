package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchSource;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.model.VerificationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecordMatchRequest(
        @NotNull(message = "{validation.format.notNull}")
        GameFormat format,

        @NotNull(message = "{validation.platform.notNull}")
        Platform platform,

        MatchStructure matchStructure,

        Instant playedAt,

        Integer round,

        @Size(max = 1000, message = "{validation.matchNotes.size}")
        String notes,

        MatchSource source,

        VerificationStatus verificationStatus,

        UUID tournamentParticipationId,

        @Valid
        TournamentParticipationRequest tournament,

        @NotNull(message = "{validation.seat1.notNull}")
        @Valid
        MatchParticipantRequest seat1,

        @NotNull(message = "{validation.seat2.notNull}")
        @Valid
        MatchParticipantRequest seat2,

        String scorePreset,

        Boolean isIntentionalDraw,

        List<@Valid GameRecordRequest> games
) {
}
