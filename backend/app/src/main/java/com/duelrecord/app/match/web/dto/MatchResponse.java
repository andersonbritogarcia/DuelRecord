package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchSource;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.model.VerificationStatus;
import com.duelrecord.app.match.persistence.model.Match;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        GameFormat format,
        UUID tournamentParticipationId,
        Instant playedAt,
        Platform platform,
        MatchStructure matchStructure,
        Integer round,
        String notes,
        MatchSource source,
        VerificationStatus verificationStatus,
        UUID winnerPlayerId,
        boolean isDraw,
        boolean isIntentionalDraw,
        UUID createdBy,
        List<MatchParticipantResponse> participants,
        List<GameResponse> games,
        Instant createdAt,
        Instant updatedAt
) {
    public static MatchResponse fromEntity(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getFormat(),
                match.getTournamentParticipationId(),
                match.getPlayedAt(),
                match.getPlatform(),
                match.getMatchStructure(),
                match.getRound(),
                match.getNotes(),
                match.getSource(),
                match.getVerificationStatus(),
                match.getWinnerPlayerId(),
                match.isDraw(),
                match.isIntentionalDraw(),
                match.getCreatedBy(),
                match.getParticipants() != null
                        ? match.getParticipants().stream().map(MatchParticipantResponse::fromEntity).toList()
                        : List.of(),
                match.getGames() != null
                        ? match.getGames().stream().map(GameResponse::fromEntity).toList()
                        : List.of(),
                match.getCreatedAt(),
                match.getUpdatedAt()
        );
    }
}
