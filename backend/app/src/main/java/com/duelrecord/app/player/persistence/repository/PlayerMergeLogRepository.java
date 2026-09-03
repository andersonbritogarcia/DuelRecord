package com.duelrecord.app.player.persistence.repository;

import com.duelrecord.app.player.persistence.model.PlayerMergeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerMergeLogRepository extends JpaRepository<PlayerMergeLog, UUID> {

    List<PlayerMergeLog> findByTargetUserIdOrderByMergedAtDesc(UUID targetUserId);

    List<PlayerMergeLog> findByMergedGhostPlayerId(UUID mergedGhostPlayerId);
}
