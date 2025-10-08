package com.sgp.parish.service;

import com.sgp.parish.dto.ParishRequest;
import com.sgp.parish.dto.ParishResponse;
import java.util.List;

public interface ParishService {
    ParishResponse createParish(ParishRequest request);
    ParishResponse getParishById(Long id);
    List<ParishResponse> getAllParishes();
    ParishResponse updateParish(Long id, ParishRequest request);
    void deleteParish(Long id);
}