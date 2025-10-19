package com.sgp.sacrament.certificate.service;

/**
 * Servicio encargado de la generación del documento PDF.
 */
public interface CertificatePdfService {

    /**
     * Genera el acta sacramental de un ID dado como un array de bytes PDF.
     * @param sacramentId ID del registro sacramental a certificar.
     * @return Array de bytes del documento PDF.
     */
    byte[] generateSacramentActaPdf(Long sacramentId);
}