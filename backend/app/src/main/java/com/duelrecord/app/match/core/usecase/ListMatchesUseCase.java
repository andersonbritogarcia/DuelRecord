package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.persistence.model.Match;
import com.duelrecord.app.match.persistence.repository.MatchRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListMatchesUseCase implements UseCase<ListMatchesInput, Page<Match>> {

    private final MatchRepository matchRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Match> execute(ListMatchesInput input) {
        Pageable pageable = input.pageable() != null ? input.pageable() : Pageable.unpaged();
        Page<Match> page = matchRepository.findFiltered(input.playerId(), input.format(), input.platform(), pageable);
        page.forEach(m -> {
            m.getParticipants().forEach(p -> {});
            m.getGames().forEach(g -> {});
        });
        return page;
    }
}
