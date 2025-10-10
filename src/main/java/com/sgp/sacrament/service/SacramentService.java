package com.sgp.sacrament.service;

import com.sgp.sacrament.dto.SacramentRequest;
import com.sgp.sacrament.dto.SacramentResponse;

import java.util.List;

public interface SacramentService {

    /** Crea un nuevo registro de sacramento y sus detalles. */
    SacramentResponse createSacrament(SacramentRequest request);

    /** Obtiene un sacramento por su ID. */
    SacramentResponse getSacramentById(Long id);

    /** * Obtiene todos los sacramentos recibidos por la persona asociada al usuario autenticado.
     */
    List<SacramentResponse> getMySacraments(); // ⭐ NUEVO MÉTODO ⭐

    /** Obtiene todos los sacramentos registrados. */
    List<SacramentResponse> getAllSacraments();

    /** Obtiene todos los sacramentos recibidos por una persona específica. */
    List<SacramentResponse> getSacramentsByPersonId(Long personId);

    /** Actualiza un registro de sacramento y sus detalles. */
    SacramentResponse updateSacrament(Long id, SacramentRequest request);

    /** Elimina (lógicamente) un registro de sacramento. */
    void deleteSacrament(Long id);
}

