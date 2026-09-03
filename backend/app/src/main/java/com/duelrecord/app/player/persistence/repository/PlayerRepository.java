package com.duelrecord.app.player.persistence.repository;

import com.duelrecord.app.player.persistence.model.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findByUserId(UUID userId);

    @Query("""
            SELECT p FROM Player p
            WHERE (:query IS NULL OR :query = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.mtgoUsername) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.arenaUsername) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY
                CASE
                    WHEN :query IS NOT NULL AND LOWER(p.displayName) = LOWER(:query) THEN 0
                    WHEN :query IS NOT NULL AND LOWER(p.displayName) LIKE LOWER(CONCAT(:query, '%')) THEN 1
                    ELSE 2
                END,
                p.displayName ASC
            """)
    Page<Player> search(@Param("query") String query, Pageable pageable);
}
