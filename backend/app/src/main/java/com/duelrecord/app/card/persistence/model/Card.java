package com.duelrecord.app.card.persistence.model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "scryfall_id", nullable = false, unique = true, length = 36)
    private String scryfallId;

    @Column(name = "oracle_id", nullable = false, length = 36)
    private String oracleId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type_line", nullable = false)
    private String typeLine;

    @Column(name = "mana_cost", length = 100)
    private String manaCost;

    @Column(name = "cmc", nullable = false, precision = 5, scale = 1)
    private BigDecimal cmc;

    @Column(name = "color_identity", nullable = false, length = 10)
    private String colorIdentity;

    @Column(name = "image_uri_small", columnDefinition = "TEXT")
    private String imageUriSmall;

    @Column(name = "image_uri_normal", columnDefinition = "TEXT")
    private String imageUriNormal;

    @Column(name = "image_uri_art_crop", columnDefinition = "TEXT")
    private String imageUriArtCrop;

    @Column(name = "is_commander_legal", nullable = false)
    private boolean commanderLegal;

    @Column(name = "is_partner", nullable = false)
    private boolean partner;

    @Column(name = "is_companion", nullable = false)
    private boolean companion;

    @Column(name = "is_background", nullable = false)
    private boolean background;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Card create(
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
            boolean commanderLegal,
            boolean partner,
            boolean companion,
            boolean background
    ) {
        Instant now = Instant.now();
        return Card.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .scryfallId(scryfallId)
                .oracleId(oracleId)
                .name(name)
                .typeLine(typeLine)
                .manaCost(manaCost)
                .cmc(cmc != null ? cmc : BigDecimal.ZERO)
                .colorIdentity(colorIdentity != null ? colorIdentity : "")
                .imageUriSmall(imageUriSmall)
                .imageUriNormal(imageUriNormal)
                .imageUriArtCrop(imageUriArtCrop)
                .commanderLegal(commanderLegal)
                .partner(partner)
                .companion(companion)
                .background(background)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateFromScryfall(
            String oracleId,
            String name,
            String typeLine,
            String manaCost,
            BigDecimal cmc,
            String colorIdentity,
            String imageUriSmall,
            String imageUriNormal,
            String imageUriArtCrop,
            boolean commanderLegal,
            boolean partner,
            boolean companion,
            boolean background
    ) {
        this.oracleId = oracleId;
        this.name = name;
        this.typeLine = typeLine;
        this.manaCost = manaCost;
        this.cmc = cmc != null ? cmc : BigDecimal.ZERO;
        this.colorIdentity = colorIdentity != null ? colorIdentity : "";
        this.imageUriSmall = imageUriSmall;
        this.imageUriNormal = imageUriNormal;
        this.imageUriArtCrop = imageUriArtCrop;
        this.commanderLegal = commanderLegal;
        this.partner = partner;
        this.companion = companion;
        this.background = background;
        this.updatedAt = Instant.now();
    }
}
