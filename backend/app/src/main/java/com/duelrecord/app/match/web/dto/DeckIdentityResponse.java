package com.duelrecord.app.match.web.dto;

import com.duelrecord.app.match.persistence.model.DeckIdentity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record DeckIdentityResponse(UUID id,
                                   String name,
                                   String colorIdentity,
                                   String signature,
                                   List<DeckIdentityCardResponse> cards,
                                   Instant createdAt) {

    public static DeckIdentityResponse fromEntity(DeckIdentity entity) {
        List<DeckIdentityCardResponse> cardResponses = Objects.nonNull(entity.getCards()) ?
                entity.getCards().stream().map(DeckIdentityCardResponse::fromEntity).toList() :
                List.of();

        return new DeckIdentityResponse(entity.getId(),
                                        entity.getName(),
                                        entity.getColorIdentity(),
                                        entity.getSignature(),
                                        cardResponses,
                                        entity.getCreatedAt());
    }
}
