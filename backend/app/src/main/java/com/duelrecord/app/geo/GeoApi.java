package com.duelrecord.app.geo;

import com.duelrecord.app.geo.core.usecase.FindCityByIdUseCase;
import com.duelrecord.app.geo.core.usecase.GetOrCreateCityInput;
import com.duelrecord.app.geo.core.usecase.GetOrCreateCityUseCase;
import com.duelrecord.app.geo.core.usecase.ListCountriesUseCase;
import com.duelrecord.app.geo.core.usecase.SearchCitiesInput;
import com.duelrecord.app.geo.core.usecase.SearchCitiesUseCase;
import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.model.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeoApi {

    private final ListCountriesUseCase listCountriesUseCase;
    private final SearchCitiesUseCase searchCitiesUseCase;
    private final GetOrCreateCityUseCase getOrCreateCityUseCase;
    private final FindCityByIdUseCase findCityByIdUseCase;

    public List<Country> listCountries() {
        return listCountriesUseCase.execute();
    }

    public Page<City> searchCities(String countryCode, String query, Pageable pageable) {
        return searchCitiesUseCase.execute(new SearchCitiesInput(countryCode, query, pageable));
    }

    public City getOrCreateCity(String countryCode, String name) {
        return getOrCreateCityUseCase.execute(new GetOrCreateCityInput(countryCode, name));
    }

    public Optional<City> findCityById(UUID id) {
        return findCityByIdUseCase.execute(id);
    }
}
