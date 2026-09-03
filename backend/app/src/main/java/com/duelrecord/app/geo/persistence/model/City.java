package com.duelrecord.app.geo.persistence.model;

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
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "state_province", length = 50)
    private String stateProvince;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static City create(Country country, String name, String stateProvince) {
        Instant now = Instant.now();
        return City.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .country(country)
                .name(name)
                .stateProvince(stateProvince)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void update(String name, String stateProvince) {
        this.name = name != null ? name : this.name;
        this.stateProvince = stateProvince;
        this.updatedAt = Instant.now();
    }
}
