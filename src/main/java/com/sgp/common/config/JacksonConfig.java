package com.sgp.common.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Registro del módulo para soportar LocalDateTime y otros tipos de fechas de Java 8
        Module javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);

        // Deshabilitar la característica que exige un "handler" para los tipos de fecha de Java 8
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }
}