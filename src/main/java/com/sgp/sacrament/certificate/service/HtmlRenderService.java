package com.sgp.sacrament.certificate.service;

import org.thymeleaf.context.Context;

/**
 * Define la funcionalidad para renderizar una plantilla de Thymeleaf a una cadena HTML.
 */
public interface HtmlRenderService {

    /**
     * Procesa una plantilla de Thymeleaf con el contexto dado.
     * @param templateName Nombre de la plantilla (ej. 'certificados/acta_bautismo').
     * @param context Objeto de contexto de Thymeleaf que contiene las variables.
     * @return La plantilla renderizada como una cadena HTML.
     */
    String render(String templateName, Context context);
}