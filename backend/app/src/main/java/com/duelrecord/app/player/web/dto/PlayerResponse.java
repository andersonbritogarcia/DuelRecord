package com.duelrecord.app.player.web.dto;

import com.duelrecord.app.geo.web.dto.CityResponse;
import com.duelrecord.app.player.persistence.model.Player;

import java.time.Instant;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        UUID userId,
        String name,
        String displayName,
        String mtgoUsername,
        String arenaUsername,
        boolean isGhost,
        CityResponse city,
        Instant createdAt,
        Instant updatedAt
) {

    public static PlayerResponse fromDomain(Player player) {
        if (player == null) {
            return null;
        }
        return new PlayerResponse(
                player.getId(),
                player.getUserId(),
                player.getName(),
                player.getDisplayName(),
                player.getMtgoUsername(),
                player.getArenaUsername(),
                player.isGhost(),
                CityResponse.fromDomain(player.getCity()),
                player.getCreatedAt(),
                player.getUpdatedAt()
        );
    }
}
