package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindCityByIdUseCaseTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private FindCityByIdUseCase useCase;

    @Test
    void shouldReturnEmptyWhenIdIsNull() {
        assertTrue(useCase.execute(null).isEmpty());
    }

    @Test
    void shouldReturnCityWhenFound() {
        var id = UUID.randomUUID();
        var city = City.create(Country.create("BR", "Brazil"), "Recife", "PE");

        when(cityRepository.findById(id)).thenReturn(Optional.of(city));

        var result = useCase.execute(id);

        assertTrue(result.isPresent());
        assertEquals("Recife", result.get().getName());
    }
}
