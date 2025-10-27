package com.sgp.common.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6; // Código de 6 dígitos alfanuméricos

    /**
     * Genera un código de verificación alfanumérico seguro.
     */
    public String generateAlphanumericCode() {
        SecureRandom random = new SecureRandom();

        // Genera una cadena de 6 caracteres aleatorios del conjunto definido
        return random.ints(CODE_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    /**
     * Genera un token seguro basado en UUID (usado para Magic Links o tokens de larga duración/un solo uso).
     * @return String con el token UUID.
     */
    public String generateSecureToken() {
        // Un UUID V4 es un token criptográficamente seguro y único, ideal para Magic Links.
        return UUID.randomUUID().toString();
    }
}