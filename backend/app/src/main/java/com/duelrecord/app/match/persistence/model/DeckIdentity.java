package com.duelrecord.app.match.persistence.model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deck_identities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeckIdentity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color_identity", nullable = false, length = 10)
    private String colorIdentity;

    @Column(name = "signature", nullable = false, unique = true, length = 64)
    private String signature;

    @OneToMany(mappedBy = "deckIdentity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeckIdentityCard> cards = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static DeckIdentity create(String name, String colorIdentity, String signature) {
        Instant now = Instant.now();
        return DeckIdentity.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .name(name)
                .colorIdentity(colorIdentity)
                .signature(signature)
                .cards(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void addCard(DeckIdentityCard card) {
        this.cards.add(card);
        card.setDeckIdentity(this);
    }
}
