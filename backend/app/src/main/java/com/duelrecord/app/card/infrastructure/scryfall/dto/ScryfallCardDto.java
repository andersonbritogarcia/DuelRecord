package com.duelrecord.app.card.infrastructure.scryfall.dto;

import com.duelrecord.app.card.core.util.ColorIdentityUtils;
import com.duelrecord.app.card.persistence.model.Card;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        if (Objects.nonNull(typeLine)) {
            return typeLine;
        }
        if (Objects.nonNull(cardFaces) && !cardFaces.isEmpty()) {
            return cardFaces.stream()
                    .map(ScryfallCardFaceDto::getTypeLine)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" // "));
        }
        return "Card";
    }

    public String resolveManaCost() {
        if (Objects.nonNull(manaCost)) {
            return manaCost;
        }
        if (Objects.nonNull(cardFaces) && !cardFaces.isEmpty() && Objects.nonNull(cardFaces.get(0).getManaCost())) {
            return cardFaces.get(0).getManaCost();
        }
        return null;
    }

    public String resolveOracleText() {
        if (Objects.nonNull(oracleText)) {
            return oracleText;
        }
        if (Objects.nonNull(cardFaces) && !cardFaces.isEmpty()) {
            return cardFaces.stream()
                    .map(ScryfallCardFaceDto::getOracleText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));
        }
        return null;
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
                resolveManaCost(),
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
                resolveManaCost(),
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
        String effectiveType = resolveTypeLine();
        if (Objects.isNull(effectiveType)) {
            return false;
        }
        String lowerType = effectiveType.toLowerCase();
        boolean isLegendaryCreature = lowerType.contains("legendary") && lowerType.contains("creature");
        boolean isLegendaryPlaneswalker = lowerType.contains("legendary") && lowerType.contains("planeswalker");
        String effectiveOracle = resolveOracleText();
        boolean isCanBeCommander = Objects.nonNull(effectiveOracle) && effectiveOracle.toLowerCase().contains("can be your commander");
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
        String effectiveOracle = resolveOracleText();
        if (Objects.nonNull(effectiveOracle)) {
            String lowerOracle = effectiveOracle.toLowerCase();
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
        String effectiveOracle = resolveOracleText();
        return Objects.nonNull(effectiveOracle) && effectiveOracle.toLowerCase().contains("companion —");
    }

    public boolean isBackgroundMechanic() {
        String effectiveType = resolveTypeLine();
        return Objects.nonNull(effectiveType) && effectiveType.toLowerCase().contains("background");
    }
}
