package com.duelrecord.app.card.infrastructure.scryfall.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallImageUrisDto {

    private String small;
    private String normal;
    private String large;
    private String png;

    @JsonProperty("art_crop")
    private String artCrop;

    @JsonProperty("border_crop")
    private String borderCrop;
}
