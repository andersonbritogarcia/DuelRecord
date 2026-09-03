package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SearchPlayersUseCase implements UseCase<SearchPlayersInput, Page<Player>> {

    private final PlayerRepository playerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Player> execute(SearchPlayersInput input) {
        Pageable pageable = Objects.nonNull(input) && Objects.nonNull(input.pageable())
                ? input.pageable()
                : PageRequest.of(0, 20);

        String query = Objects.nonNull(input) ? input.query() : null;

        return playerRepository.search(query, pageable);
    }
}
