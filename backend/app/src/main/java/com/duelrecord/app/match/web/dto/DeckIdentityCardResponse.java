package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.core.model.DeckCardRole;
import com.duelrecord.app.match.persistence.model.DeckIdentityCard;

import java.util.UUID;

public record DeckIdentityCardResponse(UUID id, UUID cardId, DeckCardRole role) {
    public static DeckIdentityCardResponse fromEntity(DeckIdentityCard card) {
        return new DeckIdentityCardResponse(card.getId(), card.getCardId(), card.getRole());
    }
}