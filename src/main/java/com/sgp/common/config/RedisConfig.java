package com.sgp.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de Redis para habilitar las notificaciones de eventos de espacio de claves (keyspace events).
 * Esto es necesario para que KeyExpirationEventMessageListener (UserInactivityListener) funcione.
 */
@Configuration
public class RedisConfig {

    // 1. Template para contadores (Login Attempts)
    @Bean("redisIntegerTemplate")
    public RedisTemplate<String, Integer> redisIntegerTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new  GenericToStringSerializer<>(Integer.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(Integer.class));
        template.afterPropertiesSet();
        return template;
    }

    // 2. Template para claves de String (Inactividad, Genérico)
    @Bean("redisStringTemplate")
    public RedisTemplate<String, String> redisStringTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Contenedor para manejar las notificaciones de eventos de Redis (ej. TTL caducado).
     * Esto es NECESARIO para la suspensión por inactividad.
     */
    @Bean
    public RedisMessageListenerContainer keyExpirationListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);

        // ⭐ CORRECCIÓN CRUCIAL: Habilitar la emisión de eventos de expiración en Redis.
        // La "Ex" significa Events of Expiration (Eventos de Expiración).
        listenerContainer.getConnectionFactory().getConnection().setConfig("notify-keyspace-events", "Ex");

        // NOTA IMPORTANTE: Para entornos de producción, se recomienda configurar
        // 'notify-keyspace-events "Ex"' directamente en el archivo redis.conf.

        return listenerContainer;
    }
}
