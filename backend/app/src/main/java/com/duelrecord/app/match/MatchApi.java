package com.duelrecord.app.match;

import com.duelrecord.app.match.core.usecase.FindMatchByIdUseCase;
import com.duelrecord.app.match.core.usecase.ListMatchesInput;
import com.duelrecord.app.match.core.usecase.ListMatchesUseCase;
import com.duelrecord.app.match.core.usecase.RecordMatchInput;
import com.duelrecord.app.match.core.usecase.RecordMatchUseCase;
import com.duelrecord.app.match.persistence.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchApi {

    private final RecordMatchUseCase recordMatchUseCase;
    private final FindMatchByIdUseCase findMatchByIdUseCase;
    private final ListMatchesUseCase listMatchesUseCase;

    public Match recordMatch(RecordMatchInput input) {
        return recordMatchUseCase.execute(input);
    }

    public Optional<Match> findMatchById(UUID id) {
        return findMatchByIdUseCase.execute(id);
    }

    public Page<Match> listMatches(ListMatchesInput input) {
        return listMatchesUseCase.execute(input);
    }
}
