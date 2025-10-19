package com.sgp.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION = 15; // minutos

    public static final String CONTEXT_VERIFY = "verify";
    public static final String CONTEXT_RESET = "reset";

    private final RedisTemplate<String, Integer> redisTemplate;

    private String buildKey(String email, String context) {
        return "otp:" + context + ":" + email;
    }

    public void recordFailedAttempt(String email, String context) {
        String key = buildKey(email, context);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        if (attempts == null) {
            redisTemplate.opsForValue().set(key, 1, BLOCK_DURATION, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().set(key, attempts + 1, BLOCK_DURATION, TimeUnit.MINUTES);
        }
    }

    public void recordSuccessfulAttempt(String email, String context) {
        String key = buildKey(email, context);
        redisTemplate.delete(key);
    }

    public boolean isBlocked(String email, String context) {
        String key = buildKey(email, context);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    public long getRemainingBlockTime(String email, String context) {
        String key = buildKey(email, context);
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }

    //metodo altenrativop :
    public void checkBlockedOrThrow(String email, String context) {
        if (isBlocked(email, context)) {
            long seconds = getRemainingBlockTime(email, context);
            throw new LockedException("Demasiados intentos fallidos. Intenta en " + seconds + " segundos.");
        }
    }

}
