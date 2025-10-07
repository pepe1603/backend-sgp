package com.sgp.common.queue;

import com.sgp.common.service.MailService; // ⬅️ Inyectar la interfaz de envío real
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener; // ⬅️ Anotación clave
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailConsumer {

    // Inyectamos el servicio de correo real (la implementación síncrona/con reintentos)
    private final MailService mailService;

    // Define qué cola debe escuchar
    @RabbitListener(queues = "${queue.mail.name:mailQueue}")
    public void handleMailMessage(MailMessage message) {
        log.info("Mensaje de correo recibido de la cola. Procesando envío a: {}", message.getTo());

        try {
            // Llama al servicio de envío de correo, que ahora opera en el hilo del consumidor
            mailService.sendHtmlMail(
                    message.getTo(),
                    message.getSubject(),
                    message.getTemplateName(),
                    message.getModel()
            );
            log.info("Correo enviado exitosamente a: {}", message.getTo());

        } catch (Exception e) {
            log.error("Fallo CRÍTICO al procesar el mensaje para {}. Error: {}. El mensaje será reencolado (o movido a DLQ).", message.getTo(), e.getMessage());
            // Nota: Por defecto, RabbitMQ reencolará un mensaje fallido (lo que provee el reintento).
            // Para una gestión robusta, se configura una Dead Letter Exchange (DLX).
            throw new RuntimeException("Fallo en el procesamiento, requiere reintento.", e);
        }
    }
}
