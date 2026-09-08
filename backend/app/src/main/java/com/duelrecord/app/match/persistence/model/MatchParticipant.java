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
@Table(name = "match_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchParticipant {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "deck_identity_id", nullable = false)
    private UUID deckIdentityId;

    @Column(name = "seat", nullable = false)
    private int seat;

    @Column(name = "display_name_snapshot", nullable = false)
    private String displayNameSnapshot;

    @Column(name = "is_winner", nullable = false)
    private boolean isWinner;

    @Column(name = "game_wins", nullable = false)
    private int gameWins;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static MatchParticipant create(
            UUID playerId,
            UUID deckIdentityId,
            int seat,
            String displayNameSnapshot,
            boolean isWinner,
            int gameWins
    ) {
        return MatchParticipant.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .playerId(playerId)
                .deckIdentityId(deckIdentityId)
                .seat(seat)
                .displayNameSnapshot(displayNameSnapshot)
                .isWinner(isWinner)
                .gameWins(gameWins)
                .createdAt(Instant.now())
                .build();
    }
}
