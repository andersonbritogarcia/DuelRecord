package com.duelrecord.app.match.core.validator;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.shared.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatRulesValidatorTest {

    private FormatRulesValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FormatRulesValidator();
    }

    @Test
    @DisplayName("Duel Commander should allow PAPER and MTGO platforms")
    void duelCommanderAllowsPaperAndMtgo() {
        assertThatCode(() -> validator.validatePlatform(GameFormat.DUEL_COMMANDER, Platform.PAPER))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validatePlatform(GameFormat.DUEL_COMMANDER, Platform.MTGO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Duel Commander should reject ARENA platform")
    void duelCommanderRejectsArena() {
        assertThatThrownBy(() -> validator.validatePlatform(GameFormat.DUEL_COMMANDER, Platform.ARENA))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.invalidPlatform");
    }

    @Test
    @DisplayName("Duel Commander 500 should allow PAPER and MTGO platforms")
    void duelCommander500AllowsPaperAndMtgo() {
        assertThatCode(() -> validator.validatePlatform(GameFormat.DUEL_COMMANDER_500, Platform.PAPER))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validatePlatform(GameFormat.DUEL_COMMANDER_500, Platform.MTGO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Duel Commander 500 should reject ARENA platform")
    void duelCommander500RejectsArena() {
        assertThatThrownBy(() -> validator.validatePlatform(GameFormat.DUEL_COMMANDER_500, Platform.ARENA))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.invalidPlatform");
    }

    @Test
    @DisplayName("Brawl should allow ARENA platform")
    void brawlAllowsArena() {
        assertThatCode(() -> validator.validatePlatform(GameFormat.BRAWL, Platform.ARENA))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = Platform.class, names = {"PAPER", "MTGO"})
    @DisplayName("Brawl should reject PAPER and MTGO platforms")
    void brawlRejectsPaperAndMtgo(Platform platform) {
        assertThatThrownBy(() -> validator.validatePlatform(GameFormat.BRAWL, platform))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.match.invalidPlatform");
    }

    @Test
    @DisplayName("Default match structure should be BO3 for DC and DC500, and BO1 for Brawl")
    void defaultMatchStructures() {
        assertThat(validator.resolveMatchStructure(GameFormat.DUEL_COMMANDER, null)).isEqualTo(MatchStructure.BO3);
        assertThat(validator.resolveMatchStructure(GameFormat.DUEL_COMMANDER_500, null)).isEqualTo(MatchStructure.BO3);
        assertThat(validator.resolveMatchStructure(GameFormat.BRAWL, null)).isEqualTo(MatchStructure.BO1);
    }

    @Test
    @DisplayName("Explicit match structure should be preserved when provided")
    void explicitMatchStructurePreserved() {
        assertThat(validator.resolveMatchStructure(GameFormat.BRAWL, MatchStructure.BO3)).isEqualTo(MatchStructure.BO3);
        assertThat(validator.resolveMatchStructure(GameFormat.DUEL_COMMANDER, MatchStructure.BO1)).isEqualTo(MatchStructure.BO1);
    }
}
