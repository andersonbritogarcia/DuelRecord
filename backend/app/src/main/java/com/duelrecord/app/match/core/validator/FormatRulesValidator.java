package com.duelrecord.app.match.core.validator;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.shared.exceptions.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class FormatRulesValidator {

    private static final Map<GameFormat, Set<Platform>> ALLOWED_PLATFORMS = Map.of(
            GameFormat.DUEL_COMMANDER, EnumSet.of(Platform.PAPER, Platform.MTGO),
            GameFormat.DUEL_COMMANDER_500, EnumSet.of(Platform.PAPER, Platform.MTGO),
            GameFormat.BRAWL, EnumSet.of(Platform.ARENA)
    );

    private static final Map<GameFormat, MatchStructure> DEFAULT_STRUCTURES = Map.of(
            GameFormat.DUEL_COMMANDER, MatchStructure.BO3,
            GameFormat.DUEL_COMMANDER_500, MatchStructure.BO3,
            GameFormat.BRAWL, MatchStructure.BO1
    );

    public void validatePlatform(GameFormat format, Platform platform) {
        if (format == null || platform == null) {
            return;
        }
        Set<Platform> allowed = ALLOWED_PLATFORMS.get(format);
        if (allowed != null && !allowed.contains(platform)) {
            throw new BusinessException("problem.match.invalidPlatform", platform, format);
        }
    }

    public MatchStructure resolveMatchStructure(GameFormat format, MatchStructure requested) {
        if (requested != null) {
            return requested;
        }
        return DEFAULT_STRUCTURES.getOrDefault(format, MatchStructure.BO3);
    }
}
