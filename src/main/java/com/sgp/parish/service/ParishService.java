package com.sgp.parish.service;

import com.sgp.parish.dto.ParishRequest;
import com.sgp.parish.dto.ParishResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParishService {
    ParishResponse createParish(ParishRequest request);
    ParishResponse getParishById(Long id);
    // ⭐ MODIFICADO: Reemplazamos List<ParishResponse> por Page<ParishResponse> y añadimos Pageable.
    /**
     * Obtiene una lista paginada de parroquias.
     * @param pageable Objeto de paginación y ordenamiento.
     * @return Una Page de ParishResponse.
     */
    Page<ParishResponse> findAllParishes(Pageable pageable);

    ParishResponse updateParish(Long id, ParishRequest request);
    void deleteParish(Long id);
}