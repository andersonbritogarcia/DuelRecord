package com.duelrecord.app.geo.web.dto;

import com.duelrecord.app.geo.persistence.model.City;

import java.util.UUID;

public record CityResponse(UUID id, CountryResponse country, String name) {

    public static CityResponse fromDomain(City city) {
        if (city == null) {
            return null;
        }
        return new CityResponse(
                city.getId(),
                CountryResponse.fromDomain(city.getCountry()),
                city.getName()
        );
    }
}
