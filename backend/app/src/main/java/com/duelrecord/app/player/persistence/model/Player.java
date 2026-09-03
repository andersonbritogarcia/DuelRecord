package com.duelrecord.app.player.persistence.model;

import com.duelrecord.app.geo.persistence.model.City;
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
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "mtgo_username", length = 100)
    private String mtgoUsername;

    @Column(name = "arena_username", length = 100)
    private String arenaUsername;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isGhost() {
        return this.userId == null;
    }

    public static Player createRegistered(UUID userId, String name, String displayName) {
        Instant now = Instant.now();
        String resolvedName = name != null ? name : "Player";
        String resolvedDisplayName = displayName != null ? displayName : resolvedName;

        return Player.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .userId(userId)
                .name(resolvedName)
                .displayName(resolvedDisplayName)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Player createGhost(String name, String displayName, String mtgoUsername, String arenaUsername, City city) {
        Instant now = Instant.now();
        String resolvedDisplayName = displayName != null ? displayName : name;

        return Player.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .userId(null)
                .name(name)
                .displayName(resolvedDisplayName)
                .mtgoUsername(mtgoUsername)
                .arenaUsername(arenaUsername)
                .city(city)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateProfile(String displayName, String mtgoUsername, String arenaUsername, City city) {
        if (displayName != null) {
            this.displayName = displayName;
        }
        this.mtgoUsername = mtgoUsername;
        this.arenaUsername = arenaUsername;
        this.city = city;
        this.updatedAt = Instant.now();
    }
}
