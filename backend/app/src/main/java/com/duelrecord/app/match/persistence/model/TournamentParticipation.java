package com.duelrecord.app.match.persistence.model;

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
@Table(name = "tournament_participations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentParticipation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "tournament_name", nullable = false, length = 150)
    private String tournamentName;

    @Column(name = "placement")
    private Integer placement;

    @Column(name = "store_name", length = 150)
    private String storeName;

    @Column(name = "swiss_rounds")
    private Integer swissRounds;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static TournamentParticipation create(
            UUID playerId,
            String tournamentName,
            Integer placement,
            String storeName,
            Integer swissRounds,
            String notes,
            Instant playedAt
    ) {
        Instant now = Instant.now();
        return TournamentParticipation.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .playerId(playerId)
                .tournamentName(tournamentName)
                .placement(placement)
                .storeName(storeName)
                .swissRounds(swissRounds)
                .notes(notes)
                .playedAt(playedAt != null ? playedAt : now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
