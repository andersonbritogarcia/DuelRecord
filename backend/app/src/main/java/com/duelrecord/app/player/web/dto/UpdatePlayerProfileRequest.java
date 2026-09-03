package com.duelrecord.app.player.web.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdatePlayerProfileRequest(
        @Size(min = 1, max = 255, message = "{validation.displayName.size}")
        String displayName,

        @Size(max = 100, message = "{validation.mtgoUsername.size}")
        String mtgoUsername,

        @Size(max = 100, message = "{validation.arenaUsername.size}")
        String arenaUsername,

        UUID cityId,

        @Size(min = 2, max = 2, message = "{validation.countryCode.size}")
        String newCountryCode,

        @Size(max = 100, message = "{validation.cityName.size}")
        String newCityName
) {
}
