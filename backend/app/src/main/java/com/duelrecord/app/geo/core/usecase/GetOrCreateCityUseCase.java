package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import com.duelrecord.app.geo.persistence.repository.CountryRepository;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GetOrCreateCityUseCase implements UseCase<GetOrCreateCityInput, City> {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public City execute(GetOrCreateCityInput input) {
        ValidationUtils.requireNonNull(input, "problem.invalidCityData.detail");
        ValidationUtils.requireNonBlank(input.countryCode(), "problem.invalidCountryCode.detail");
        ValidationUtils.requireNonBlank(input.name(), "problem.invalidCityName.detail");

        String countryCode = input.countryCode().toUpperCase();
        String name = input.name();
        String stateProvince = Objects.nonNull(input.stateProvince()) ? input.stateProvince().toUpperCase() : null;

        Country country = countryRepository.findById(countryCode)
                .orElseThrow(() -> new EntityNotFoundException("problem.countryNotFound.detail", countryCode));

        return cityRepository.findByCountryCodeAndNameAndStateProvinceIgnoreCase(countryCode, name, stateProvince)
                .orElseGet(() -> cityRepository.save(City.create(country, name, stateProvince)));
    }
}
