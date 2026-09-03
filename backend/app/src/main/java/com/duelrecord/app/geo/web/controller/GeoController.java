package com.duelrecord.app.geo.web.controller;

import com.duelrecord.app.geo.core.usecase.GetOrCreateCityInput;
import com.duelrecord.app.geo.core.usecase.GetOrCreateCityUseCase;
import com.duelrecord.app.geo.core.usecase.ListCountriesUseCase;
import com.duelrecord.app.geo.core.usecase.SearchCitiesInput;
import com.duelrecord.app.geo.core.usecase.SearchCitiesUseCase;
import com.duelrecord.app.geo.web.dto.CityResponse;
import com.duelrecord.app.geo.web.dto.CountryResponse;
import com.duelrecord.app.geo.web.dto.CreateCityRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final ListCountriesUseCase listCountriesUseCase;
    private final SearchCitiesUseCase searchCitiesUseCase;
    private final GetOrCreateCityUseCase getOrCreateCityUseCase;

    @GetMapping("/countries")
    public List<CountryResponse> listCountries() {
        return listCountriesUseCase.execute()
                .stream()
                .map(CountryResponse::fromDomain)
                .toList();
    }

    @GetMapping("/cities")
    public Page<CityResponse> searchCities(@RequestParam(name = "country", required = false) String countryCode,
                                           @RequestParam(name = "q", required = false) String query,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return searchCitiesUseCase.execute(new SearchCitiesInput(countryCode, query, pageable)).map(CityResponse::fromDomain);
    }

    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse getOrCreateCity(@Valid @RequestBody CreateCityRequest request) {
        var city = getOrCreateCityUseCase.execute(new GetOrCreateCityInput(request.countryCode(), request.name()));
        return CityResponse.fromDomain(city);
    }
}
