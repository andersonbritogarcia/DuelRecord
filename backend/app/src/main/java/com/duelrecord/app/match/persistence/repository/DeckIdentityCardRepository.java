package com.duelrecord.app.match.persistence.repository;

import com.duelrecord.app.match.persistence.model.DeckIdentityCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeckIdentityCardRepository extends JpaRepository<DeckIdentityCard, UUID> {

    List<DeckIdentityCard> findByDeckIdentityId(UUID deckIdentityId);
}
