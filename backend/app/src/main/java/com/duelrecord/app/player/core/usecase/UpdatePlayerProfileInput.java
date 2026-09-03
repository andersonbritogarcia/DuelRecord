package com.duelrecord.app.player.core.usecase;

import java.util.UUID;

public record UpdatePlayerProfileInput(UUID userId,
                                       String displayName,
                                       String mtgoUsername,
                                       String arenaUsername,
                                       UUID cityId,
                                       String newCountryCode,
                                       String newCityName) {
}
