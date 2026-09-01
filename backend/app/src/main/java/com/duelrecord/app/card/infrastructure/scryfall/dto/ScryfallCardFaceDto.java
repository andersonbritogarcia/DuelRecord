package com.duelrecord.app.card.infrastructure.scryfall.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallCardFaceDto {

    private String name;

    @JsonProperty("type_line")
    private String typeLine;

    @JsonProperty("mana_cost")
    private String manaCost;

    private List<String> colors;

    @JsonProperty("oracle_text")
    private String oracleText;

    @JsonProperty("image_uris")
    private ScryfallImageUrisDto imageUris;
}
