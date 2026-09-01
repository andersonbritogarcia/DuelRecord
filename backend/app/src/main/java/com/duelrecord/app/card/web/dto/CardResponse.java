package com.duelrecord.app.card.web.dto;

import com.duelrecord.app.card.persistence.model.Card;

import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String scryfallId,
        String oracleId,
        String name,
        String typeLine,
        String manaCost,
        BigDecimal cmc,
        String colorIdentity,
        String imageUriSmall,
        String imageUriNormal,
        String imageUriArtCrop,
        boolean isCommanderLegal,
        boolean isPartner,
        boolean isCompanion,
        boolean isBackground
) {
    public static CardResponse fromDomain(Card card) {
        return new CardResponse(
                card.getId(),
                card.getScryfallId(),
                card.getOracleId(),
                card.getName(),
                card.getTypeLine(),
                card.getManaCost(),
                card.getCmc(),
                card.getColorIdentity(),
                card.getImageUriSmall(),
                card.getImageUriNormal(),
                card.getImageUriArtCrop(),
                card.isCommanderLegal(),
                card.isPartner(),
                card.isCompanion(),
                card.isBackground()
        );
    }
}
