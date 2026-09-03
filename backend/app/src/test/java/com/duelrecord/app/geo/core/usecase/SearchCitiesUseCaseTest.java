package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchCitiesUseCaseTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private SearchCitiesUseCase useCase;

    @Test
    void shouldSearchCitiesWithPagination() {
        var pageable = PageRequest.of(0, 10);
        var country = Country.create("BR", "Brazil");
        var city = City.create(country, "São Paulo");
        var page = new PageImpl<>(List.of(city), pageable, 1);

        when(cityRepository.search("BR", "São", pageable)).thenReturn(page);

        var result = useCase.execute(new SearchCitiesInput("BR", "São", pageable));

        assertEquals(1, result.getTotalElements());
        assertEquals("São Paulo", result.getContent().get(0).getName());
    }
}
