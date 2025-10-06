package com.sgp.common.service;


public interface MailService {

    /**
     * Envía un correo electrónico simple (sin plantilla).
     */
    void sendSimpleMail(String to, String subject, String text);

    /**
     * Envía un correo electrónico utilizando una plantilla de Thymeleaf.
     * @param to Destinatario
     * @param subject Asunto
     * @param templateName Nombre de la plantilla (ej. "verification-email")
     * @param model Datos para la plantilla (ej. nombre de usuario, token)
     */
    void sendHtmlMail(String to, String subject, String templateName, java.util.Map<String, Object> model);
}