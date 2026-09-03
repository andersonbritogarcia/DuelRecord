package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchCitiesUseCase implements UseCase<SearchCitiesInput, Page<City>> {

    private final CityRepository cityRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<City> execute(SearchCitiesInput input) {
        Pageable pageable = input != null && input.pageable() != null
                ? input.pageable()
                : PageRequest.of(0, 20);

        String countryCode = input != null ? input.countryCode() : null;
        String query = input != null ? input.query() : null;

        return cityRepository.search(countryCode, query, pageable);
    }
}
