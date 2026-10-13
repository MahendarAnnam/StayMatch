package org.staymatch.location.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.staymatch.location.dto.CityResponse;
import org.staymatch.location.repository.CityRepository;
import org.staymatch.location.service.CityService;

@Service
public class CityServiceImpl implements CityService {

    private static final Logger log = LoggerFactory.getLogger(CityServiceImpl.class);

    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cities")
    public List<CityResponse> getAllCities() {
        log.info("Fetching all cities from database");
        return cityRepository.findAllByOrderByNameAsc()
            .stream()
            .map(city -> new CityResponse(
                city.getId(),
                city.getName(),
                city.getLatitude(),
                city.getLongitude()
            ))
            .toList();
    }
}

