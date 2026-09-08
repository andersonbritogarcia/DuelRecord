package com.duelrecord.app.match.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateDeckIdentityRequest(
        @NotEmpty(message = "{validation.deckIdentity.items.notEmpty}")
        List<@Valid DeckIdentityCardItemRequest> items,
        @Size(max = 255, message = "{validation.deckIdentity.customName.size}")
        String customName
) {
}
