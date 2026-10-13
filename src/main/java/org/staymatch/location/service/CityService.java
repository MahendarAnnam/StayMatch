package org.staymatch.location.service;

import java.util.List;
import org.staymatch.location.dto.CityResponse;

public interface CityService {

    List<CityResponse> getAllCities();
}

