package com.duelrecord.app.geo.core.usecase;

import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.repository.CityRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindCityByIdUseCase implements UseCase<UUID, Optional<City>> {

    private final CityRepository cityRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<City> execute(UUID id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return cityRepository.findById(id);
    }
}
