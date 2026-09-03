package com.duelrecord.app.geo.persistence.repository;

import com.duelrecord.app.geo.persistence.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    @Query("""
            SELECT c FROM City c
            WHERE (:countryCode IS NULL OR :countryCode = '' OR c.country.code = UPPER(:countryCode))
              AND (:query IS NULL OR :query = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.stateProvince) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY c.name ASC, c.stateProvince ASC
            """)
    Page<City> search(@Param("countryCode") String countryCode,
                      @Param("query") String query,
                      Pageable pageable);

    @Query("""
            SELECT c FROM City c
            WHERE c.country.code = UPPER(:countryCode)
              AND LOWER(c.name) = LOWER(:name)
              AND (:stateProvince IS NULL AND c.stateProvince IS NULL OR LOWER(c.stateProvince) = LOWER(:stateProvince))
            """)
    Optional<City> findByCountryCodeAndNameAndStateProvinceIgnoreCase(
            @Param("countryCode") String countryCode,
            @Param("name") String name,
            @Param("stateProvince") String stateProvince
    );
}
