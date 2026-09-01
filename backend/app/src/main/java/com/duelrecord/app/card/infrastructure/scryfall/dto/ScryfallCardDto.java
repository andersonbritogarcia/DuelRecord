package com.duelrecord.app.card.infrastructure.scryfall.dto;

import com.duelrecord.app.card.core.util.ColorIdentityUtils;
import com.duelrecord.app.card.persistence.model.Card;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallCardDto {

    private String id;

    @JsonProperty("oracle_id")
    private String oracleId;

    private String name;

    @JsonProperty("type_line")
    private String typeLine;

    @JsonProperty("mana_cost")
    private String manaCost;

    private BigDecimal cmc;

    private List<String> colors;

    @JsonProperty("color_identity")
    private List<String> colorIdentity;

    private List<String> keywords;

    @JsonProperty("oracle_text")
    private String oracleText;

    @JsonProperty("image_uris")
    private ScryfallImageUrisDto imageUris;

    @JsonProperty("card_faces")
    private List<ScryfallCardFaceDto> cardFaces;

    public boolean isValid() {
        return Objects.nonNull(id) && Objects.nonNull(name);
    }

    public String resolveOracleId() {
        return Objects.nonNull(oracleId) ? oracleId : id;
    }

    public String resolveTypeLine() {
        return Objects.nonNull(typeLine) ? typeLine : "Card";
    }

    public String resolveColorIdentity() {
        return ColorIdentityUtils.canonicalize(colorIdentity);
    }

    public Card toDomain() {
        return Card.create(
                id,
                resolveOracleId(),
                name,
                resolveTypeLine(),
                manaCost,
                cmc,
                resolveColorIdentity(),
                resolveSmallImageUri(),
                resolveNormalImageUri(),
                resolveArtCropImageUri(),
                isCommanderEligible(),
                isPartnerMechanic(),
                isCompanionMechanic(),
                isBackgroundMechanic()
        );
    }

    public Card updateDomain(Card existing) {
        existing.updateFromScryfall(
                resolveOracleId(),
                name,
                resolveTypeLine(),
                manaCost,
                cmc,
                resolveColorIdentity(),
                resolveSmallImageUri(),
                resolveNormalImageUri(),
                resolveArtCropImageUri(),
                isCommanderEligible(),
                isPartnerMechanic(),
                isCompanionMechanic(),
                isBackgroundMechanic()
        );
        return existing;
    }

    public String resolveSmallImageUri() {
        if (Objects.nonNull(imageUris) && Objects.nonNull(imageUris.getSmall())) {
            return imageUris.getSmall();
        }
        if (Objects.nonNull(cardFaces) && !cardFaces.isEmpty() && Objects.nonNull(cardFaces.get(0).getImageUris())) {
            return cardFaces.get(0).getImageUris().getSmall();
        }
        return null;
    }

    public String resolveNormalImageUri() {
        if (Objects.nonNull(imageUris) && Objects.nonNull(imageUris.getNormal())) {
            return imageUris.getNormal();
        }
        if (Objects.nonNull(cardFaces) && !cardFaces.isEmpty() && Objects.nonNull(cardFaces.get(0).getImageUris())) {
            return cardFaces.get(0).getImageUris().getNormal();
        }
        return null;
    }

    public String resolveArtCropImageUri() {
        if (Objects.nonNull(imageUris) && Objects.nonNull(imageUris.getArtCrop())) {
            return imageUris.getArtCrop();
        }
        if (Objects.nonNull(cardFaces) && !cardFaces.isEmpty() && Objects.nonNull(cardFaces.get(0).getImageUris())) {
            return cardFaces.get(0).getImageUris().getArtCrop();
        }
        return null;
    }

    public boolean isCommanderEligible() {
        if (Objects.isNull(typeLine)) {
            return false;
        }
        String lowerType = typeLine.toLowerCase();
        boolean isLegendaryCreature = lowerType.contains("legendary") && lowerType.contains("creature");
        boolean isLegendaryPlaneswalker = lowerType.contains("legendary") && lowerType.contains("planeswalker");
        boolean isCanBeCommander = Objects.nonNull(oracleText) && oracleText.toLowerCase().contains("can be your commander");
        return isLegendaryCreature || isLegendaryPlaneswalker || isCanBeCommander;
    }

    public boolean isPartnerMechanic() {
        if (Objects.nonNull(keywords)) {
            for (String kw : keywords) {
                String lowerKw = kw.toLowerCase();
                if (lowerKw.equals("partner") || lowerKw.startsWith("partner with") || lowerKw.equals("friends forever")) {
                    return true;
                }
            }
        }
        if (Objects.nonNull(oracleText)) {
            String lowerOracle = oracleText.toLowerCase();
            return lowerOracle.contains("partner") || lowerOracle.contains("choose a background");
        }
        return false;
    }

    public boolean isCompanionMechanic() {
        if (Objects.nonNull(keywords)) {
            for (String kw : keywords) {
                if (kw.equalsIgnoreCase("companion")) {
                    return true;
                }
            }
        }
        return Objects.nonNull(oracleText) && oracleText.toLowerCase().contains("companion —");
    }

    public boolean isBackgroundMechanic() {
        return Objects.nonNull(typeLine) && typeLine.toLowerCase().contains("background");
    }
}
