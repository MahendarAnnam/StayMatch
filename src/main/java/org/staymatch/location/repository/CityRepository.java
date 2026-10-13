package org.staymatch.location.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.staymatch.common.model.City;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findAllByOrderByNameAsc();
}

