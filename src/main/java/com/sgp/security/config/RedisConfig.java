package com.sgp.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * Define el bean de RedisTemplate para manejar claves de tipo String
     * y valores de tipo Integer, necesario para LoginAttemptService.
     */
    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configura la serialización de Claves (Keys)
        // Usamos StringRedisSerializer para que las claves (ej. "login_attempt:user@test.com")
        // sean legibles en Redis.
        template.setKeySerializer(new StringRedisSerializer());

        // Configura la serialización de Valores (Values)
        // Usamos GenericToStringSerializer<Integer> para serializar los contadores
        // como cadenas de texto en Redis, lo que permite operaciones como INCREMENT.
        template.setValueSerializer(new  GenericToStringSerializer<>(Integer.class));

        // Serialización para las claves y valores de Hash (si se usan, buena práctica incluirlos)
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(Integer.class));

        template.afterPropertiesSet();
        return template;
    }
}