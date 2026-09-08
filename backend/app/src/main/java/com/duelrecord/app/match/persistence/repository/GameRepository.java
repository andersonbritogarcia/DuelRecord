package com.duelrecord.app.match.persistence.repository;

import com.duelrecord.app.match.persistence.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    List<Game> findByMatchIdOrderByGameNumberAsc(UUID matchId);
}
