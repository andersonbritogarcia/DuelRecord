package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.geo.GeoApi;
import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdatePlayerProfileUseCase implements UseCase<UpdatePlayerProfileInput, Player> {

    private final PlayerRepository playerRepository;
    private final GeoApi geoApi;

    @Override
    @Transactional
    public Player execute(UpdatePlayerProfileInput input) {
        ValidationUtils.requireNonNull(input, "problem.invalidPlayerData.detail");
        ValidationUtils.requireNonNull(input.userId(), "problem.invalidUserId.detail");

        Player player = playerRepository.findByUserId(input.userId())
                                        .orElseThrow(() -> new EntityNotFoundException("problem.playerNotFound.detail", input.userId()));

        City city = player.getCity();

        if (Objects.nonNull(input.newCountryCode()) && Objects.nonNull(input.newCityName())) {
            city = geoApi.getOrCreateCity(input.newCountryCode(), input.newCityName(), input.newStateProvince());
        } else if (Objects.nonNull(input.cityId())) {
            city = geoApi.findCityById(input.cityId())
                         .orElseThrow(() -> new EntityNotFoundException("problem.cityNotFound.detail", input.cityId()));
        }

        player.updateProfile(input.displayName(), input.mtgoUsername(), input.arenaUsername(), city);
        return playerRepository.save(player);
    }
}
