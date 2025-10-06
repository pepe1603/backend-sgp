package com.sgp.common.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
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
}