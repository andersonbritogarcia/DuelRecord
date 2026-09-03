package com.duelrecord.app.geo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCityRequest(
        @NotBlank(message = "{validation.countryCode.notBlank}")
        @Size(min = 2, max = 2, message = "{validation.countryCode.size}")
        String countryCode,

        @NotBlank(message = "{validation.cityName.notBlank}")
        @Size(max = 100, message = "{validation.cityName.size}")
        String name,

        @Size(max = 50, message = "{validation.stateProvince.size}")
        String stateProvince
) {
}
