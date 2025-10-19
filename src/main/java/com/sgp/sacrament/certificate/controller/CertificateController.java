package com.sgp.sacrament.certificate.controller;

import com.sgp.sacrament.certificate.service.CertificatePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para manejar las peticiones de generación de certificados sacramentales.
 */
@RestController
@RequestMapping("/api/v1/certificates") // Endpoint específico para certificados
@RequiredArgsConstructor
public class CertificateController {

    private final CertificatePdfService certificatePdfService;

    /**
     * Endpoint para generar el acta sacramental en formato PDF.
     * @param sacramentId El ID del registro sacramental.
     * @return ResponseEntity con el PDF como un array de bytes y las cabeceras adecuadas.
     */
    @GetMapping("/{sacramentId}/pdf")
    public ResponseEntity<byte[]> getSacramentCertificatePdf(@PathVariable Long sacramentId) {

        // 1. Generar el PDF
        byte[] pdfBytes = certificatePdfService.generateSacramentActaPdf(sacramentId);

        // 2. Configurar las cabeceras HTTP para la respuesta
        HttpHeaders headers = new HttpHeaders();

        // El contenido es un PDF
        headers.setContentType(MediaType.APPLICATION_PDF);

        // Nombre del archivo sugerido para la descarga
        String filename = "Acta_Sacramental_" + sacramentId + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);

        // Cabeceras de caché (recomendado para documentos generados dinámicamente)
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        // 3. Devolver la respuesta
        // El cuerpo de la respuesta son los bytes del PDF
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }
}