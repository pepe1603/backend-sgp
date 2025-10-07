package com.sgp.user.service;

import com.sgp.user.model.User;
import com.sgp.user.repository.UserRepository;
import com.sgp.user.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Para loguear la actividad de limpieza
public class CleanupService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private  final AuthService authService;

    /**
     * Tarea programada que se ejecuta cada 24 horas (media noche) para
     * eliminar usuarios que nunca verificaron su cuenta.
     * * @Scheduled(cron = "0 0 0 * * *") // Se ejecuta a media noche
     */
    @Scheduled(fixedDelay = 86400000) // 24 horas en milisegundos (para pruebas rápidas puedes usar un delay menor)
    @Transactional // Es una operación de escritura en DB
    public void removeUnverifiedUsers() {
        log.info("Iniciando tarea de limpieza de usuarios no verificados...");

        // Definimos un umbral: 2 días (48 horas)
        LocalDateTime threshold = LocalDateTime.now().minusHours(48);

        // 1. Usar el nuevo método del repositorio basado en el campo createdAt
        List<User> usersToCleanup = userRepository.findByIsEnabledFalseAndCreatedAtBefore(threshold);

        if (usersToCleanup.isEmpty()) {
            log.info("No se encontraron usuarios no verificados para eliminar.");
            return;
        }

        // 2. Eliminar tokens asociados y luego los usuarios
        usersToCleanup.forEach(user -> {
            tokenRepository.deleteByUser(user); // Elimina el token primero
            userRepository.delete(user);        // Elimina el usuario (el perfil se va en cascada)
        });

        log.info("Limpieza completada. Se eliminaron {} usuarios no verificados.", usersToCleanup.size());
    }

    // ⭐ TAREA PROGRAMADA PARA ENVIAR RECORDATORIOS (cada 24 horas) ⭐
    @Scheduled(fixedDelay = 86400000) // 24 horas
    @Transactional
    public void sendVerificationReminders() {
        log.info("Iniciando tarea de envío de recordatorios de verificación...");

        // 1. Definir el umbral de recordatorio: 24 horas después del registro
        LocalDateTime reminderThreshold = LocalDateTime.now().minusHours(24);
        LocalDateTime cleanupThreshold = LocalDateTime.now().minusHours(48);

        // 2. Buscar usuarios que no están verificados, que tienen más de 24h de registro,
        // y que NO HAN ALCANZADO el umbral de eliminación de 48h.
        List<User> usersToRemind = userRepository.findByIsEnabledFalseAndCreatedAtBefore(reminderThreshold)
                .stream()
                // Filtramos los que están a punto de ser eliminados (48h) para no duplicar acciones
                .filter(user -> user.getCreatedAt().isAfter(cleanupThreshold))
                .collect(Collectors.toList());


        if (usersToRemind.isEmpty()) {
            log.info("No se encontraron usuarios para enviar recordatorios.");
            return;
        }

        usersToRemind.forEach(user -> {
            try {
                // Reutilizamos el método de reenvío que genera un nuevo token y lo envía.
                // Aunque se llama "resend", actúa como un recordatorio aquí.
                authService.resendVerificationCode(user.getEmail());
                log.info("Recordatorio de verificación enviado a: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Fallo al enviar recordatorio a {}: {}", user.getEmail(), e.getMessage());
            }
        });

        log.info("Envío de recordatorios completado. Se enviaron {} recordatorios.", usersToRemind.size());
    }
}