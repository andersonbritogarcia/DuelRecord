package com.duelrecord.app.match.persistence.model;

import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.MatchSource;
import com.duelrecord.app.match.core.model.MatchStructure;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.model.VerificationStatus;
import com.fasterxml.uuid.Generators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 50)
    private GameFormat format;

    @Column(name = "tournament_participation_id")
    private UUID tournamentParticipationId;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_structure", nullable = false, length = 30)
    private MatchStructure matchStructure;

    @Column(name = "round")
    private Integer round;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private MatchSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private VerificationStatus verificationStatus;

    @Column(name = "winner_player_id")
    private UUID winnerPlayerId;

    @Column(name = "is_draw", nullable = false)
    private boolean isDraw;

    @Column(name = "is_intentional_draw", nullable = false)
    private boolean isIntentionalDraw;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("seat ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<MatchParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("gameNumber ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<Game> games = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Match create(
            GameFormat format,
            UUID tournamentParticipationId,
            Instant playedAt,
            Platform platform,
            MatchStructure matchStructure,
            Integer round,
            String notes,
            MatchSource source,
            VerificationStatus verificationStatus,
            UUID winnerPlayerId,
            boolean isDraw,
            boolean isIntentionalDraw,
            UUID createdBy
    ) {
        Instant now = Instant.now();
        return Match.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .format(format)
                .tournamentParticipationId(tournamentParticipationId)
                .playedAt(playedAt != null ? playedAt : now)
                .platform(platform)
                .matchStructure(matchStructure)
                .round(round)
                .notes(notes)
                .source(source != null ? source : MatchSource.MANUAL)
                .verificationStatus(verificationStatus != null ? verificationStatus : VerificationStatus.UNVERIFIED)
                .winnerPlayerId(winnerPlayerId)
                .isDraw(isDraw)
                .isIntentionalDraw(isIntentionalDraw)
                .createdBy(createdBy)
                .participants(new ArrayList<>())
                .games(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void addParticipant(MatchParticipant participant) {
        this.participants.add(participant);
        participant.setMatch(this);
    }

    public void addGame(Game game) {
        this.games.add(game);
        game.setMatch(this);
    }
}
