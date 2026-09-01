package com.duelrecord.app.card.infrastructure.scryfall.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallSearchResponseDto {

    private String object;

    @JsonProperty("total_cards")
    private int totalCards;

    @JsonProperty("has_more")
    private boolean hasMore;

    private List<ScryfallCardDto> data;

    public List<ScryfallCardDto> getData() {
        return data != null ? data : Collections.emptyList();
    }
}
