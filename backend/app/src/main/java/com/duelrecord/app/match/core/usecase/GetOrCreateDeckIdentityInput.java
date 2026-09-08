package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.core.model.DeckCardRole;

import java.util.List;
import java.util.UUID;

public record GetOrCreateDeckIdentityInput(
        List<CardRoleInput> items,
        String customName
) {
    public record CardRoleInput(UUID cardId, DeckCardRole role) {}

    public GetOrCreateDeckIdentityInput(List<CardRoleInput> items) {
        this(items, null);
    }
}
