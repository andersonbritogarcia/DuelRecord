package com.duelrecord.app.player;

import com.duelrecord.app.player.core.usecase.CreateGhostPlayerInput;
import com.duelrecord.app.player.core.usecase.CreateGhostPlayerUseCase;
import com.duelrecord.app.player.core.usecase.EnsurePlayerForUserInput;
import com.duelrecord.app.player.core.usecase.EnsurePlayerForUserUseCase;
import com.duelrecord.app.player.core.usecase.FindPlayerByIdUseCase;
import com.duelrecord.app.player.core.usecase.GetPlayerByUserIdUseCase;
import com.duelrecord.app.player.core.usecase.SearchPlayersInput;
import com.duelrecord.app.player.core.usecase.SearchPlayersUseCase;
import com.duelrecord.app.player.persistence.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerApi {

    private final FindPlayerByIdUseCase findPlayerByIdUseCase;
    private final GetPlayerByUserIdUseCase getPlayerByUserIdUseCase;
    private final EnsurePlayerForUserUseCase ensurePlayerForUserUseCase;
    private final CreateGhostPlayerUseCase createGhostPlayerUseCase;
    private final SearchPlayersUseCase searchPlayersUseCase;

    public Optional<Player> findPlayerById(UUID id) {
        return findPlayerByIdUseCase.execute(id);
    }

    public Optional<Player> getPlayerByUserId(UUID userId) {
        return getPlayerByUserIdUseCase.execute(userId);
    }

    public Player ensurePlayerForUser(UUID userId, String email, String name) {
        return ensurePlayerForUserUseCase.execute(new EnsurePlayerForUserInput(userId, email, name));
    }

    public Player createGhostPlayer(String name, String displayName, String mtgoUsername, String arenaUsername, UUID cityId) {
        return createGhostPlayerUseCase.execute(new CreateGhostPlayerInput(name, displayName, mtgoUsername, arenaUsername, cityId));
    }

    public Page<Player> searchPlayers(String query, Pageable pageable) {
        return searchPlayersUseCase.execute(new SearchPlayersInput(query, pageable));
    }
}
