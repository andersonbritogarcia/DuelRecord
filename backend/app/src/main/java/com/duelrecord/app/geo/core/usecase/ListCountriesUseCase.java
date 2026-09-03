package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CountryRepository;
import com.duelrecord.app.shared.usecase.NullaryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCountriesUseCase implements NullaryUseCase<List<Country>> {

    private final CountryRepository countryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Country> execute() {
        return countryRepository.findAllByOrderByNameAsc();
    }
}
