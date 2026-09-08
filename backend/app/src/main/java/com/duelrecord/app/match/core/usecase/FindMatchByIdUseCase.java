package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.persistence.model.Match;
import com.duelrecord.app.match.persistence.repository.MatchRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindMatchByIdUseCase implements UseCase<UUID, Optional<Match>> {

    private final MatchRepository matchRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Match> execute(UUID id) {
        Optional<Match> match = matchRepository.findWithParticipantsById(id);
        match.ifPresent(m -> {
            m.getParticipants().forEach(p -> {});
            m.getGames().forEach(g -> {});
        });
        return match;
    }
}
