package com.sgp.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String email;
    private String message;
    private boolean requiresVerification; // Indicará al frontend que debe esperar un código
}