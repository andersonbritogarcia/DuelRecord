package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnsurePlayerForUserUseCase implements UseCase<EnsurePlayerForUserInput, Player> {

    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public Player execute(EnsurePlayerForUserInput input) {
        ValidationUtils.requireNonNull(input, "problem.invalidUserData.detail");
        ValidationUtils.requireNonNull(input.userId(), "problem.invalidUserId.detail");

        return playerRepository.findByUserId(input.userId()).orElseGet(() -> {
            String resolvedName = resolveName(input.name(), input.email());
            Player player = Player.createRegistered(input.userId(), resolvedName, resolvedName);
            return playerRepository.save(player);
        });
    }

    private String resolveName(String name, String email) {
        if (!ValidationUtils.isBlank(name)) {
            return name.strip();
        }

        if (!ValidationUtils.isBlank(email)) {
            int atIndex = email.indexOf('@');
            if (atIndex > 0) {
                return email.substring(0, atIndex).strip();
            }
            return email.strip();
        }
        
        return "Player";
    }
}
