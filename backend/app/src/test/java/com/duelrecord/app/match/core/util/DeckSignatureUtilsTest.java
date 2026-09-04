package com.duelrecord.app.match.core.util;

import com.duelrecord.app.match.core.model.DeckCardRole;
import com.duelrecord.app.match.core.util.DeckSignatureUtils.SignatureItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeckSignatureUtilsTest {

    @Test
    @DisplayName("Should throw exception when generating signature for empty items")
    void shouldThrowWhenEmpty() {
        assertThatThrownBy(() -> DeckSignatureUtils.generateSignature(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DeckSignatureUtils.generateSignature(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should generate deterministic signature regardless of item order")
    void shouldGenerateDeterministicSignature() {
        UUID card1 = UUID.randomUUID();
        UUID card2 = UUID.randomUUID();

        List<SignatureItem> orderA = List.of(
                new SignatureItem(card1, DeckCardRole.COMMANDER),
                new SignatureItem(card2, DeckCardRole.PARTNER)
        );

        List<SignatureItem> orderB = List.of(
                new SignatureItem(card2, DeckCardRole.PARTNER),
                new SignatureItem(card1, DeckCardRole.COMMANDER)
        );

        String sigA = DeckSignatureUtils.generateSignature(orderA);
        String sigB = DeckSignatureUtils.generateSignature(orderB);

        assertThat(sigA).isEqualTo(sigB);
        assertThat(sigA).hasSize(64); // SHA-256 hex string length
    }

    @Test
    @DisplayName("Should produce different signatures for different roles with same cards")
    void shouldProduceDifferentSignaturesForDifferentRoles() {
        UUID card1 = UUID.randomUUID();

        String sigCommander = DeckSignatureUtils.generateSignature(List.of(new SignatureItem(card1, DeckCardRole.COMMANDER)));
        String sigCompanion = DeckSignatureUtils.generateSignature(List.of(new SignatureItem(card1, DeckCardRole.COMPANION)));

        assertThat(sigCommander).isNotEqualTo(sigCompanion);
    }
}
