package com.duelrecord.app.geo.core.usecase;

import org.springframework.data.domain.Pageable;

public record SearchCitiesInput(String countryCode, String query, Pageable pageable) {
}
