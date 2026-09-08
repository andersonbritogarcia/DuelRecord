package com.duelrecord.app.match.persistence.model;

import com.duelrecord.app.match.core.model.DeckCardRole;
import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deck_identity_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeckIdentityCard {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_identity_id", nullable = false)
    private DeckIdentity deckIdentity;

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private DeckCardRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static DeckIdentityCard create(UUID cardId, DeckCardRole role) {
        return DeckIdentityCard.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .cardId(cardId)
                .role(role)
                .createdAt(Instant.now())
                .build();
    }
}
