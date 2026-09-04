package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.core.model.DeckCardRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeckIdentityCardItemRequest(@NotNull(message = "{validation.cardId.notNull}") UUID cardId,
                                          @NotNull(message = "{validation.deckCardRole.notNull}") DeckCardRole role) {
}