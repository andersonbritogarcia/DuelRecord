package com.duelrecord.app.match.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MatchParticipantRequest(
        @NotNull(message = "{validation.playerId.notNull}")
        UUID playerId,

        @NotNull(message = "{validation.deckIdentityId.notNull}")
        UUID deckIdentityId
) {
}
