package com.duelrecord.app.match.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record TournamentParticipationRequest(
        @NotBlank(message = "{validation.tournamentName.notBlank}")
        @Size(max = 150, message = "{validation.tournamentName.size}")
        String tournamentName,
        Integer placement,
        @Size(max = 150, message = "{validation.storeName.size}")
        String storeName,
        Integer swissRounds,
        String notes,
        Instant playedAt
) {
}
