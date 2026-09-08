package com.duelrecord.app.card.infrastructure.scryfall.dto;

import com.duelrecord.app.card.persistence.model.Card;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScryfallCardDtoTest {

    @Test
    @DisplayName("Should correctly map a transform / multi-faced card like Brigid")
    void shouldMapTransformCardFaces() {
        ScryfallCardFaceDto frontFace = new ScryfallCardFaceDto();
        frontFace.setName("Brigid, Clachan's Heart");
        frontFace.setTypeLine("Legendary Creature — Kithkin Warrior");
        frontFace.setManaCost("{2}{W}");
        frontFace.setColors(List.of("W"));
        frontFace.setOracleText("Whenever this creature enters or transforms...");
        ScryfallImageUrisDto frontImages = new ScryfallImageUrisDto();
        frontImages.setSmall("https://cards.scryfall.io/small/front.jpg");
        frontImages.setNormal("https://cards.scryfall.io/normal/front.jpg");
        frontImages.setArtCrop("https://cards.scryfall.io/art_crop/front.jpg");
        frontFace.setImageUris(frontImages);

        ScryfallCardFaceDto backFace = new ScryfallCardFaceDto();
        backFace.setName("Brigid, Doun's Mind");
        backFace.setTypeLine("Legendary Creature — Kithkin Soldier");
        backFace.setOracleText("{T}: Add X {G} or X {W}...");

        ScryfallCardDto dto = new ScryfallCardDto();
        dto.setId("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8");
        dto.setOracleId("60fc89a1-cc6f-4a32-84c3-09abf7a4a1df");
        dto.setName("Brigid, Clachan's Heart // Brigid, Doun's Mind");
        dto.setTypeLine("Legendary Creature — Kithkin Warrior // Legendary Creature — Kithkin Soldier");
        dto.setCmc(BigDecimal.valueOf(3.0));
        dto.setColorIdentity(List.of("G", "W"));
        dto.setCardFaces(List.of(frontFace, backFace));

        assertTrue(dto.isValid());
        assertTrue(dto.isCommanderEligible());
        assertEquals("{2}{W}", dto.resolveManaCost());
        assertEquals("WG", dto.resolveColorIdentity());
        assertEquals("https://cards.scryfall.io/small/front/jpg".replace("/jpg", ".jpg"), dto.resolveSmallImageUri());

        Card card = dto.toDomain();
        assertNotNull(card);
        assertEquals("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8", card.getScryfallId());
        assertEquals("60fc89a1-cc6f-4a32-84c3-09abf7a4a1df", card.getOracleId());
        assertEquals("Brigid, Clachan's Heart // Brigid, Doun's Mind", card.getName());
        assertEquals("{2}{W}", card.getManaCost());
        assertEquals("WG", card.getColorIdentity());
        assertTrue(card.isCommanderLegal());
    }
}
