package com.sgp.sacrament.certificate.service;

import com.lowagie.text.pdf.BaseFont;
import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.sacrament.certificate.dto.SacramentCertificateDTO;
import com.sgp.sacrament.model.Sacrament;
import com.sgp.sacrament.repository.SacramentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CertificatePdfServiceImpl implements CertificatePdfService {

    private final SacramentRepository sacramentRepository;
    private final CertificateMapper certificateMapper;
    private final HtmlRenderService htmlRenderService;

    @Override
    public byte[] generateSacramentActaPdf(Long sacramentId) {
        // 1. Buscar el registro sacramental y verificar su existencia
        Sacrament sacrament = sacramentRepository.findById(sacramentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sacramento", "ID-SACRAMENT", sacramentId));

        // 2. Mapear la entidad a un DTO simple para la vista
        SacramentCertificateDTO actaDTO = certificateMapper.toCertificateDTO(sacrament);

        // 3. Determinar el nombre de la plantilla
        // Usamos el tipo de sacramento para seleccionar la plantilla adecuada
        String templateName = "sacramentos/" + actaDTO.getTipoSacramento().name().toLowerCase() + "_template";
        // Ejemplo: Si tipoSacramento es BAPTISM, la plantilla será 'sacramentos/baptism_template'

        // 4. Preparar el Contexto de Thymeleaf
        Context context = new Context(Locale.forLanguageTag("es")); // Usar español para formatos de fecha
        context.setVariable("acta", actaDTO);

        // 5. Renderizar la plantilla HTML
        String htmlContent = htmlRenderService.render(templateName, context);

        // 6. Convertir HTML a PDF (Usando Flying Saucer/ITextRenderer)
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // Requerido para manejar UTF-8 y caracteres especiales como tildes
            //renderer.getFontResolver().addFont("fonts/arial.ttf", BaseFont.IDENTITY_H, false);
            // NOTA: Si usas una fuente específica como Times New Roman, debes asegurarte de que ITextRenderer pueda acceder a su archivo .ttf
            renderer.getFontResolver().addFont("src/main/resources/fonts/Lora-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("src/main/resources/fonts/PlayfairDisplay-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            renderer.setDocumentFromString(htmlContent);
            renderer.layout(); // Realiza el cálculo del layout
            renderer.createPDF(os); // Genera el PDF en el OutputStream

            return os.toByteArray();
        } catch (IOException e) {
            // Manejo de error de IO al escribir el PDF
            throw new RuntimeException("Error de I/O al generar el PDF del acta.", e);
        } catch (Exception e) {
            // Manejo de errores de Flying Saucer
            throw new RuntimeException("Error durante la conversión HTML a PDF con Flying Saucer.", e);
        }
    }
}