package com.duelrecord.app.card.persistence.repository;

import com.duelrecord.app.card.persistence.model.Card;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByScryfallId(String scryfallId);

    List<Card> findByScryfallIdIn(Collection<String> scryfallIds);

    Optional<Card> findByOracleId(String oracleId);

    Optional<Card> findByNameIgnoreCase(String name);

    @Query(value = """
            SELECT c.* FROM cards c
            WHERE (:query IS NULL OR :query = '' OR c.search_vector @@ web_search_to_tsquery('english', :query) OR c.name ILIKE CONCAT('%', :query, '%'))
              AND (:colorIdentity IS NULL OR :colorIdentity = '' OR c.color_identity = :colorIdentity)
              AND (:commanderLegal IS NULL OR c.is_commander_legal = :commanderLegal)
            ORDER BY (CASE WHEN :query IS NOT NULL AND :query != '' THEN ts_rank(c.search_vector, web_search_to_tsquery('english', :query)) ELSE 0 END) DESC, c.name ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Card> searchFullText(@Param("query") String query,
                              @Param("colorIdentity") String colorIdentity,
                              @Param("commanderLegal") Boolean commanderLegal,
                              @Param("limit") int limit);

    @Query("""
            SELECT c FROM Card c
            WHERE (:query IS NULL OR :query = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.typeLine) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR c.oracleId IN (SELECT c2.oracleId FROM Card c2 WHERE LOWER(c2.name) LIKE LOWER(CONCAT('%', :query, '%'))))
              AND (:colorIdentity IS NULL OR :colorIdentity = '' OR c.colorIdentity = :colorIdentity)
              AND (:commanderLegal IS NULL OR c.commanderLegal = :commanderLegal)
            ORDER BY c.name ASC
            """)
    List<Card> searchJpa(@Param("query") String query,
                         @Param("colorIdentity") String colorIdentity,
                         @Param("commanderLegal") Boolean commanderLegal,
                         Pageable pageable);
}
