package com.duelrecord.app.geo.web.dto;

import com.duelrecord.app.geo.persistence.model.Country;

public record CountryResponse(String code, String name) {

    public static CountryResponse fromDomain(Country country) {
        if (country == null) {
            return null;
        }
        return new CountryResponse(country.getCode(), country.getName());
    }
}
