package com.sgp.common.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate; // ⬅️ Inyectar la plantilla de AMQP
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailProducer {

    private final AmqpTemplate rabbitTemplate;

    // Nombre de la cola (debe coincidir en la configuración del consumidor)
    @Value("${queue.mail.name:mailQueue}")
    private String routingKey; // Ahora es la clave de ruteo (el nombre de la cola)

    // Inyectar el nombre del exchange
    @Value("${queue.mail.exchange:mailExchange}")
    private String exchangeName; // ⬅️ NUEVO


    public void sendMailMessage(String to, String subject, String templateName, Map<String, Object> model) {

        MailMessage message = new MailMessage(to, subject, templateName, model);
        // El envío a la cola ocurre instantáneamente, liberando el hilo del API.
        // Enviar al exchange, usando el nombre de la cola como clave de ruteo
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message); // ⬅️ Cambio
        log.info("Mensaje de correo encolado a RabbitMQ. Destinatario: {}", to);
    }
}