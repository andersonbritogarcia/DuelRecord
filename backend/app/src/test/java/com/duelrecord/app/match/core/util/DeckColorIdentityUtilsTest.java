package com.duelrecord.app.match.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckColorIdentityUtilsTest {

    @Test
    @DisplayName("Should return 'C' when input collection is empty or null")
    void shouldReturnColorlessWhenEmptyOrNull() {
        assertThat(DeckColorIdentityUtils.combine(null)).isEqualTo("C");
        assertThat(DeckColorIdentityUtils.combine(List.of())).isEqualTo("C");
        assertThat(DeckColorIdentityUtils.combine(List.of("", "   "))).isEqualTo("C");
    }

    @Test
    @DisplayName("Should return single color canonically")
    void shouldReturnSingleColor() {
        assertThat(DeckColorIdentityUtils.combine(List.of("W"))).isEqualTo("W");
        assertThat(DeckColorIdentityUtils.combine(List.of("g"))).isEqualTo("G");
    }

    @Test
    @DisplayName("Should combine Yoshimaru (W) + Kraum (UR) into WUR")
    void shouldCombineYoshimaruAndKraum() {
        String result = DeckColorIdentityUtils.combine(List.of("W", "UR"));
        assertThat(result).isEqualTo("WUR");
    }

    @Test
    @DisplayName("Should combine Tasigur (UBG) + Lutri (UR) into UBRG")
    void shouldCombineTasigurAndLutri() {
        String result = DeckColorIdentityUtils.combine(List.of("UBG", "UR"));
        assertThat(result).isEqualTo("UBRG");
    }

    @Test
    @DisplayName("Should always sort in canonical WUBRG order regardless of input order")
    void shouldSortInWubrgOrder() {
        String result = DeckColorIdentityUtils.combine(List.of("G", "R", "B", "U", "W"));
        assertThat(result).isEqualTo("WUBRG");
    }

    @Test
    @DisplayName("Should return 'C' when card has colorless identity or invalid chars")
    void shouldReturnColorlessForKozilekOrKarn() {
        assertThat(DeckColorIdentityUtils.combine(List.of("C"))).isEqualTo("C");
        assertThat(DeckColorIdentityUtils.combine(List.of(""))).isEqualTo("C");
    }
}
