package com.duelrecord.app.match.persistence.model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "game_number", nullable = false)
    private int gameNumber;

    @Column(name = "winner_player_id")
    private UUID winnerPlayerId;

    @Column(name = "starting_player_id")
    private UUID startingPlayerId;

    @Column(name = "is_draw", nullable = false)
    private boolean isDraw;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Game create(
            int gameNumber,
            UUID winnerPlayerId,
            UUID startingPlayerId,
            boolean isDraw,
            Integer durationSeconds,
            String notes
    ) {
        return Game.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .gameNumber(gameNumber)
                .winnerPlayerId(winnerPlayerId)
                .startingPlayerId(startingPlayerId)
                .isDraw(isDraw)
                .durationSeconds(durationSeconds)
                .notes(notes)
                .createdAt(Instant.now())
                .build();
    }
}
