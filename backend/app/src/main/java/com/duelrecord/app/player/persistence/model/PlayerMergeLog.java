package com.duelrecord.app.player.persistence.model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_merge_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerMergeLog {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Column(name = "merged_ghost_player_id", nullable = false)
    private UUID mergedGhostPlayerId;

    @Column(name = "original_ghost_name", nullable = false)
    private String originalGhostName;

    @Column(name = "approved_by_admin_id")
    private UUID approvedByAdminId;

    @Column(name = "merged_at", nullable = false, updatable = false)
    private Instant mergedAt;

    public static PlayerMergeLog create(UUID targetUserId, UUID mergedGhostPlayerId, String originalGhostName, UUID approvedByAdminId) {
        return PlayerMergeLog.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .targetUserId(targetUserId)
                .mergedGhostPlayerId(mergedGhostPlayerId)
                .originalGhostName(originalGhostName)
                .approvedByAdminId(approvedByAdminId)
                .mergedAt(Instant.now())
                .build();
    }
}
