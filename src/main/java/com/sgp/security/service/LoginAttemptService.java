package com.sgp.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    // Constantes de Limite y Tiempo
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_MINUTES = 30; // Tiempo de bloqueo

    // Prefijo para las claves de Redis: login_attempt:{email}
    private static final String KEY_PREFIX = "login_attempt:";

    private final RedisTemplate<String, Integer> redisTemplate;

    private String buildKey(String email) {
        return KEY_PREFIX + email;
    }

    // --- Métodos de Control ---

    /**
     * Incrementa el contador de fallos para un email.
     */
    public void recordFailedAttempt(String email) {
        String key = buildKey(email);

        // 1. Incrementar el contador (o inicializarlo a 1)
        Integer attempts = redisTemplate.opsForValue().increment(key, 1).intValue();

        // 2. Si el contador es 1, establecer la expiración
        if (attempts == 1) {
            // El contador expira en 30 minutos (para que los intentos se reinicien)
            redisTemplate.expire(key, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }

        // 3. Si se alcanza el límite, bloquear al usuario
        if (attempts >= MAX_ATTEMPTS) {
            lockUser(key);
        }
    }

    /**
     * Bloquea la cuenta reestableciendo la expiración del contador a 30 minutos
     * y asegurando que se mantenga el máximo de intentos.
     */
    private void lockUser(String key) {
        // Aseguramos que el contador sea MAX_ATTEMPTS para que isBlocked() sea TRUE
        redisTemplate.opsForValue().set(key, MAX_ATTEMPTS);

        // La clave expirará en 30 minutos, desbloqueando la cuenta automáticamente
        redisTemplate.expire(key, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Resetea el contador de fallos (llamado tras un login exitoso).
     */
    public void recordSuccessfulAttempt(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }

    /**
     * Verifica si la cuenta está bloqueada.
     */
    public boolean isBlocked(String email) {
        String key = KEY_PREFIX + email;
        Integer attempts = redisTemplate.opsForValue().get(key);

        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    public long getRemainingBlockTime(String email) {
        String key = buildKey(email);
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }

    //metodo altenrativop :
    public void checkBlockedOrThrow(String email) {
        if (isBlocked(email)) {
            long seconds = getRemainingBlockTime(email);
            throw new LockedException("Demasiados intentos fallidos. Intenta en " + seconds + " segundos.");
        }
    }



}