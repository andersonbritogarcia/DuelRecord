package com.duelrecord.app.match.persistence.repository;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.persistence.model.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    @EntityGraph(attributePaths = {"participants"})
    @Query("SELECT m FROM Match m WHERE m.id = :id")
    Optional<Match> findWithParticipantsById(@Param("id") UUID id);

    @Query(value = """
        SELECT DISTINCT m FROM Match m
        LEFT JOIN m.participants p
        WHERE (:playerId IS NULL OR p.playerId = :playerId)
          AND (:format IS NULL OR m.format = :format)
          AND (:platform IS NULL OR m.platform = :platform)
        ORDER BY m.playedAt DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT m) FROM Match m
        LEFT JOIN m.participants p
        WHERE (:playerId IS NULL OR p.playerId = :playerId)
          AND (:format IS NULL OR m.format = :format)
          AND (:platform IS NULL OR m.platform = :platform)
    """)
    Page<Match> findFiltered(
            @Param("playerId") UUID playerId,
            @Param("format") GameFormat format,
            @Param("platform") Platform platform,
            Pageable pageable
    );

    Page<Match> findByTournamentParticipationId(UUID tournamentParticipationId, Pageable pageable);
}
