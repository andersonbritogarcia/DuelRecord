package com.duelrecord.app.player.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateGhostPlayerRequest(
        @NotBlank(message = "{validation.playerName.notBlank}")
        @Size(max = 255, message = "{validation.playerName.size}")
        String name,

        @Size(max = 255, message = "{validation.displayName.size}")
        String displayName,

        @Size(max = 100, message = "{validation.mtgoUsername.size}")
        String mtgoUsername,

        @Size(max = 100, message = "{validation.arenaUsername.size}")
        String arenaUsername,

        UUID cityId
) {
}
