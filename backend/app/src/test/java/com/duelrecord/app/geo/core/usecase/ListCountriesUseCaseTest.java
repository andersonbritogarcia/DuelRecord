package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CountryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCountriesUseCaseTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private ListCountriesUseCase useCase;

    @Test
    void shouldReturnAllCountriesSortedByName() {
        var countries = List.of(
                Country.create("BR", "Brazil"),
                Country.create("US", "United States")
        );

        when(countryRepository.findAllByOrderByNameAsc()).thenReturn(countries);

        var result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals("BR", result.get(0).getCode());
        assertEquals("Brazil", result.get(0).getName());
    }
}
