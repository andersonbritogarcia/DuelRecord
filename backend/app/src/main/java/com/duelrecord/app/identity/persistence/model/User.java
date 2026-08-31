package com.duelrecord.app.identity.persistence.model;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "auth_provider_id", nullable = false, unique = true)
    private String authProviderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public User(String email, String authProviderId) {
        Instant now = Instant.now();
        this.id = Generators.timeBasedEpochGenerator().generate();
        this.email = email;
        this.authProviderId = authProviderId;
        this.status = UserStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public boolean hasSameEmail(String otherEmail) {
        return this.email.equalsIgnoreCase(otherEmail);
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }
}
