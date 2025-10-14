package com.sgp.sacrament.service;

import com.sgp.sacrament.dto.SacramentRequest;
import com.sgp.sacrament.dto.SacramentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SacramentService {

    /** Crea un nuevo registro de sacramento y sus detalles. */
    SacramentResponse createSacrament(SacramentRequest request);

    /** Obtiene un sacramento por su ID. */
    SacramentResponse getSacramentById(Long id);

    /** * Obtiene todos los sacramentos recibidos por la persona asociada al usuario autenticado.
     */
    List<SacramentResponse> getMySacraments(); // ⭐ NUEVO MÉTODO ⭐

    // ⭐ MODIFICADO/UNIFICADO: Reemplaza getAllSacraments() y getSacramentsByPersonId() para gestión.
    /**
     * Obtiene una lista paginada de sacramentos, con filtro opcional por ID de persona.
     * @param personId ID opcional de la Persona para filtrar.
     * @param pageable Objeto de paginación y ordenamiento.
     * @return Una Page de SacramentResponse.
     */
    Page<SacramentResponse> findAllSacraments(Long personId, Pageable pageable);

    /** Obtiene todos los sacramentos recibidos por una persona específica. */
    List<SacramentResponse> getSacramentsByPersonId(Long personId);

    /** Actualiza un registro de sacramento y sus detalles. */
    SacramentResponse updateSacrament(Long id, SacramentRequest request);

    /** Elimina (lógicamente) un registro de sacramento. */
    void deleteSacrament(Long id);
}

