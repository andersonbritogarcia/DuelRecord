package com.duelrecord.app.match.persistence.repository;

import com.duelrecord.app.match.persistence.model.MatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, UUID> {

    List<MatchParticipant> findByMatchIdOrderBySeatAsc(UUID matchId);

    List<MatchParticipant> findByPlayerId(UUID playerId);
}
