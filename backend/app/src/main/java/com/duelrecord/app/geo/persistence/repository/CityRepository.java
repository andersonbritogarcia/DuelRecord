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
              AND (:query IS NULL OR :query = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY c.name ASC
            """)
    Page<City> search(@Param("countryCode") String countryCode,
                      @Param("query") String query,
                      Pageable pageable);

    @Query("""
            SELECT c FROM City c
            WHERE c.country.code = UPPER(:countryCode)
              AND LOWER(c.name) = LOWER(:name)
            """)
    Optional<City> findByCountryCodeAndNameIgnoreCase(
            @Param("countryCode") String countryCode,
            @Param("name") String name
    );
}
