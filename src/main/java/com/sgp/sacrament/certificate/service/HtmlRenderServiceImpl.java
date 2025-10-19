package com.sgp.sacrament.certificate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Implementación de HtmlRenderService que utiliza el motor de plantillas de Spring/Thymeleaf.
 */
@Service
@RequiredArgsConstructor
public class HtmlRenderServiceImpl implements HtmlRenderService {

    // SpringTemplateEngine es el componente central de Thymeleaf en Spring Boot
    private final SpringTemplateEngine templateEngine;

    @Override
    public String render(String templateName, Context context) {
        // templateEngine.process toma el nombre de la plantilla (sin la extensión .html)
        // y el contexto de variables, y devuelve el HTML renderizado.
        return templateEngine.process(templateName, context);
    }
}