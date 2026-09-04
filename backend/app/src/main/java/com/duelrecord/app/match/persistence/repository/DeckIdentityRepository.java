package com.duelrecord.app.match.persistence.repository;

import com.duelrecord.app.match.persistence.model.DeckIdentity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeckIdentityRepository extends JpaRepository<DeckIdentity, UUID> {

    @EntityGraph(attributePaths = {"cards"})
    Optional<DeckIdentity> findBySignature(String signature);

    @EntityGraph(attributePaths = {"cards"})
    Optional<DeckIdentity> findWithCardsById(UUID id);
}
