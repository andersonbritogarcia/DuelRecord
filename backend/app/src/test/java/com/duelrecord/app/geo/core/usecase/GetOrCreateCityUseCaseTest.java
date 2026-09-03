package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import com.duelrecord.app.geo.persistence.repository.CountryRepository;
import com.duelrecord.app.shared.exceptions.BusinessException;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrCreateCityUseCaseTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private GetOrCreateCityUseCase useCase;

    @Test
    void shouldThrowWhenInputIsNull() {
        assertThrows(BusinessException.class, () -> useCase.execute(null));
    }

    @Test
    void shouldThrowWhenCountryCodeIsBlank() {
        var input = new GetOrCreateCityInput("", "City");
        assertThrows(BusinessException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenCityNameIsBlank() {
        var input = new GetOrCreateCityInput("BR", "");
        assertThrows(BusinessException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenCountryNotFound() {
        var input = new GetOrCreateCityInput("XX", "Unknown");
        when(countryRepository.findById("XX")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldReturnExistingCityWhenFound() {
        var country = Country.create("BR", "Brazil");
        var existingCity = City.create(country, "Campinas");
        var input = new GetOrCreateCityInput("br", "campinas");

        when(countryRepository.findById("BR")).thenReturn(Optional.of(country));
        when(cityRepository.findByCountryCodeAndNameIgnoreCase("BR", "campinas"))
                .thenReturn(Optional.of(existingCity));

        var result = useCase.execute(input);

        assertEquals(existingCity.getId(), result.getId());
        verify(cityRepository, never()).save(any(City.class));
    }

    @Test
    void shouldCreateNewCityWhenNotFound() {
        var country = Country.create("BR", "Brazil");
        var input = new GetOrCreateCityInput("BR", "Sorocaba");

        when(countryRepository.findById("BR")).thenReturn(Optional.of(country));
        when(cityRepository.findByCountryCodeAndNameIgnoreCase("BR", "Sorocaba"))
                .thenReturn(Optional.empty());
        when(cityRepository.save(any(City.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(input);

        assertNotNull(result);
        assertEquals("Sorocaba", result.getName());
        assertEquals("BR", result.getCountry().getCode());
        verify(cityRepository).save(any(City.class));
    }
}
