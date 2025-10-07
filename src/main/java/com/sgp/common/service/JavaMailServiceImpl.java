package com.sgp.common.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j // Usar el logger
public class JavaMailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine; // Inyecta el motor de Thymeleaf

    // ⭐ HACER ASÍNCRONO ⭐
    @Override
    @Async
    public void sendSimpleMail(String to, String subject, String text) {
        // Lógica de envío...
        // ...
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@sgp.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Correo simple enviado de forma asíncrona a: {}", to);
        } catch (Exception e) {
            log.error("Fallo el envío asíncrono de correo simple a {}. Error: {}", to, e.getMessage());
            // No se relanza la excepción, sino que se registra (ver Sección 2).
        }
    }
    // ⭐ HACER ASÍNCRONO ⭐
    @Override
    @Async
    @Retryable(
            value = {RuntimeException.class}, // Tipos de excepciones que deben reintentar
            maxAttempts = 3, // Número máximo de intentos (original + 2 reintentos)
            backoff = @Backoff(delay = 2000) // Espera 2 segundos entre reintentos
    )
    public void sendHtmlMail(String to, String subject, String templateName, java.util.Map<String, Object> model) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("no-reply@sgp.com");
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(model);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Correo HTML (Plantilla: {}) enviado de forma asíncrona a: {}", templateName, to);

        } catch (Exception e) {
            // ⭐ NO RELANZAR LA EXCEPCIÓN AHORA ⭐
            // Spring Retry capturará y reintentará.
            // Relanzamos la excepción para que Retry la detecte.
            throw new RuntimeException("Fallo temporal al enviar correo: " + e.getMessage(), e);
        }
    }
    // ⭐ Nuevo Método para Capturar el Fallo Final ⭐
// Este método se ejecuta si todos los reintentos fallan.
    @Recover
    public void recover(RuntimeException e, String to, String subject, String templateName, java.util.Map<String, Object> model) {
        log.error("FALLO CRÍTICO: El envío de correo a {} falló después de {} intentos. Se requiere intervención manual o un sistema de colas.", to, 3, e);
        // Aquí es donde puedes guardar los datos en una tabla de "Correos Pendientes"
        // para ser procesados más tarde por un proceso manual o un cron job.
    }
}