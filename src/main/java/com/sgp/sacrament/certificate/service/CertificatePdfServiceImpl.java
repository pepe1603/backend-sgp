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

        // 2. Mapear la entidad a DTO para la vista
        SacramentCertificateDTO actaDTO = certificateMapper.toCertificateDTO(sacrament);

        // 3. Determinar el nombre de la plantilla HTML según el tipo de sacramento
        String templateName = "sacramentos/" + actaDTO.getTipoSacramento().name().toLowerCase() + "_template";

        // 4. Preparar el contexto de Thymeleaf
        Context context = new Context(Locale.forLanguageTag("es"));
        context.setVariable("acta", actaDTO);

        // 5. Renderizar la plantilla HTML a string
        String htmlContent = htmlRenderService.render(templateName, context);
        System.out.println("HTML generado para PDF: \n" + htmlContent);

        // 6. Generar el PDF usando Flying Saucer
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // ✅ Cargar fuentes desde el classpath (funciona en .jar y en IDE)
            renderer.getFontResolver().addFont(
                    getClass().getResource("/fonts/Lora-Regular.ttf").toString(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
            renderer.getFontResolver().addFont(
                    getClass().getResource("/fonts/PlayfairDisplay-Regular.ttf").toString(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            // ✅ Definir el baseUrl para resolver imágenes o rutas relativas en HTML
            String baseUrl = getClass().getResource("/templates/").toString();

            // ✅ Renderizar con baseUrl (solo una llamada)
            renderer.setDocumentFromString(htmlContent, baseUrl);

            renderer.layout();
            renderer.createPDF(os);

            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error de I/O al generar el PDF del acta.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error durante la conversión HTML a PDF con Flying Saucer.", e);
        }
    }
}
