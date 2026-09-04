package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.core.model.DeckCardRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateDeckIdentityRequest(@NotEmpty(message = "{validation.deckIdentity.items.notEmpty}")
                                        @Valid List<DeckIdentityCardItemRequest> items,
                                        @Size(max = 255, message = "{validation.deckIdentity.customName.size}") String customName) {
}
