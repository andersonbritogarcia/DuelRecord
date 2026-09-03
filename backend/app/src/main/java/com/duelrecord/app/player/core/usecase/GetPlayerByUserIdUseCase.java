package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlayerByUserIdUseCase implements UseCase<UUID, Optional<Player>> {

    private final PlayerRepository playerRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Player> execute(UUID userId) {
        if (Objects.isNull(userId)) {
            return Optional.empty();
        }
        return playerRepository.findByUserId(userId);
    }
}
