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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateGhostPlayerUseCase implements UseCase<CreateGhostPlayerInput, Player> {

    private final PlayerRepository playerRepository;
    private final GeoApi geoApi;

    @Override
    @Transactional
    public Player execute(CreateGhostPlayerInput input) {
        ValidationUtils.requireNonNull(input, "problem.invalidPlayerData.detail");
        ValidationUtils.requireNonBlank(input.name(), "problem.invalidPlayerName.detail");

        Player ghost = Player.createGhost(input.name(),
                                          input.displayName(),
                                          input.mtgoUsername(),
                                          input.arenaUsername(),
                                          findCityById(input.cityId()));

        return playerRepository.save(ghost);
    }

    private City findCityById(UUID id) {
        if (Objects.isNull(id)) {
            return null;
        }

        return geoApi.findCityById(id).orElseThrow(() -> new EntityNotFoundException("problem.cityNotFound.detail", id));
    }
}
