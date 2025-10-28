package com.sgp.auth.service;

import com.sgp.common.queue.MailProducer;
import com.sgp.user.model.User;
import com.sgp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling // Habilita la programación de tareas (cron jobs)
public class SuspensionService {

    private final UserRepository userRepository;
    private final MailProducer mailProducer;

    // Duración de la inactividad antes de la suspensión (1 año)
    private static final Duration SUSPENSION_THRESHOLD = Duration.ofDays(365);

    // Período de pre-aviso (1 mes antes de la suspensión)
    private static final Duration WARNING_PERIOD = Duration.ofDays(30);

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * Tarea programada que se ejecuta diariamente para buscar usuarios a punto de ser suspendidos.
     * La expresión cron '0 0 12 * * ?' significa: a las 12:00:00 PM, todos los días.
     *
     * Este método busca usuarios que están inactivos por más de 11 meses (335 días)
     * pero menos de 12 meses (365 días) y que NO han sido avisados recientemente.
     */
    @Scheduled(cron = "0 0 12 * * ?") // Se ejecuta todos los días al mediodía (12:00 PM)
    @Transactional
    public void checkAndSendInactivityWarnings() {
        log.info("Iniciando verificación programada de usuarios inactivos para pre-aviso.");

        // 1. Calcular los límites de tiempo

        // Fecha más antigua (1 año atrás): Los logins deben ser POSTERIORES a esta fecha.
        LocalDateTime suspensionThreshold = LocalDateTime.now().minus(SUSPENSION_THRESHOLD);

        // Fecha más reciente (11 meses atrás, 365 - 30 = 335 días): Los logins deben ser ANTERIORES a esta fecha.
        LocalDateTime warningThreshold = LocalDateTime.now().minus(SUSPENSION_THRESHOLD).plus(WARNING_PERIOD);

        // La query busca usuarios cuya lastLoginDate está entre suspensionThreshold y warningThreshold

        // 2. Obtener los usuarios inactivos que cruzan el umbral de pre-aviso
        // La query también verifica que lastWarningSentDate no sea reciente.
        List<User> usersToWarn = userRepository.findUsersPendingSuspensionWarning(suspensionThreshold, warningThreshold);

        if (usersToWarn.isEmpty()) {
            log.info("No se encontraron usuarios que requieran un pre-aviso de suspensión.");
            return;
        }

        log.info("Encontrados {} usuarios para enviar pre-aviso de suspensión.", usersToWarn.size());
        LocalDateTime now = LocalDateTime.now();

        for (User user : usersToWarn) {
            try {
                // 3. Enviar correo de pre-aviso
                sendInactivityWarningEmail(user);

                // 4. MARCAR al usuario en la DB como "Warning Sent" con la fecha actual
                userRepository.updateLastWarningSentDate(user.getId(), now);

                log.debug("Pre-aviso enviado y marcado para: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Fallo al enviar el pre-aviso de inactividad a {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Finalizada la verificación y envío de pre-avisos. Avisos enviados: {}", usersToWarn.size());
    }

    /**
     * Prepara y envía el correo de pre-aviso de inactividad.
     */
    private void sendInactivityWarningEmail(User user) {
        Map<String, Object> model = new HashMap<>();

        // Intenta obtener el nombre de la persona vinculada para el email (asumo un servicio auxiliar)
        // ⭐ CORRECCIÓN: Manejo seguro de Persona Nula
        String firstName = (user.getPerson() != null && user.getPerson().getFirstName() != null)
                ? user.getPerson().getFirstName()
                : "Usuario SGP"; // Nombre genérico si no hay Persona o no tiene nombre.

        model.put("firstName", firstName);

        // Calcula la fecha de suspensión
        // Es la fecha del último login más el umbral completo
        LocalDateTime suspensionDate = user.getLastLoginDate().plus(SUSPENSION_THRESHOLD);
        model.put("suspensionDate", suspensionDate);

        // Enlace para iniciar sesión y evitar la suspensión
        model.put("loginLink", frontendBaseUrl);

        mailProducer.sendMailMessage(
                user.getEmail(),
                "ADVERTENCIA: Tu Cuenta SGP Será Suspendida por Inactividad",
                "email/inactivity-warning-template",
                model
        );
    }
}
