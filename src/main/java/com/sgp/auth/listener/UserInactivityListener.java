package com.sgp.auth.listener;

import com.sgp.auth.service.AuthServiceImpl;
import com.sgp.common.queue.MailProducer;
import com.sgp.user.model.User;
import com.sgp.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Listener que reacciona a la expiración de las claves de inactividad de Redis (TTL).
 * Cuando una clave con el prefijo "inactivity:user:" expira (tras 1 año sin login),
 * este componente suspende la cuenta del usuario en la base de datos (enabled=false)
 * y envía una notificación de suspensión.
 */
@Component
@Slf4j
public class UserInactivityListener extends KeyExpirationEventMessageListener {

    private final UserRepository userRepository;
    private final MailProducer mailProducer;

    // Se inicializa con el contenedor de mensajes de Redis (inyectado en RedisConfig)
    public UserInactivityListener(RedisMessageListenerContainer listenerContainer,
                                  UserRepository userRepository,
                                  MailProducer mailProducer) {
        super(listenerContainer);
        this.userRepository = userRepository;
        this.mailProducer = mailProducer;
    }

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Evento de expiración de Redis detectado: {}", expiredKey);

        if (expiredKey.contains(AuthServiceImpl.INACTIVITY_PREFIX)) {
            try {
                // Extrae el email del usuario de la clave de Redis
                String email = expiredKey.substring(expiredKey.indexOf(AuthServiceImpl.INACTIVITY_PREFIX) + AuthServiceImpl.INACTIVITY_PREFIX.length());

                log.warn("La clave de inactividad para el usuario {} ha expirado. Procediendo a suspender la cuenta (enabled=false).", email);

                // 1. Suspender la cuenta en la DB
                userRepository.updateEnabledStatusByEmail(false, email);

                // 2. Enviar Correo de Notificación de Suspensión
                sendSuspensionNotificationEmail(email);

                log.warn("Cuenta del usuario {} suspendida exitosamente y notificación enviada.", email);

            } catch (Exception e) {
                log.error("Error al procesar la expiración de la clave {} para suspensión de cuenta: {}", expiredKey, e.getMessage(), e);
            }
        }
    }

    /**
     * Envía la notificación final de que la cuenta ha sido suspendida.
     * @param email Email del usuario suspendido.
     */
    private void sendSuspensionNotificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.error("No se pudo enviar el correo de suspensión: Usuario {} no encontrado en la DB.", email);
            return;
        }
        User user = userOpt.get();

        Map<String, Object> model = new HashMap<>();

        // Intenta obtener el nombre (asumo que User.getPerson() está disponible)
        // ⭐ CORRECCIÓN: Manejo seguro de Persona Nula
        String firstName = (user.getPerson() != null && user.getPerson().getFirstName() != null)
                ? user.getPerson().getFirstName()
                : "Usuario SGP"; // Nombre genérico si no hay Persona o no tiene nombre.
        model.put("firstName", firstName);

        mailProducer.sendMailMessage(
                user.getEmail(),
                "IMPORTANTE: Tu Cuenta SGP Ha Sido Suspendida",
                "email/suspension-notification-template", // Referencia a la plantilla
                model
        );
    }
}
