package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.Platform;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record ListMatchesInput(
        UUID playerId,
        GameFormat format,
        Platform platform,
        Pageable pageable
) {
}
