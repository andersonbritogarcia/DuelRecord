package com.duelrecord.app.geo.persistence.repository;

import com.duelrecord.app.geo.persistence.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    List<Country> findAllByOrderByNameAsc();
}
