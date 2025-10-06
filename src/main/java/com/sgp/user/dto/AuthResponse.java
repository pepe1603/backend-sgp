package com.sgp.user.dto;

import lombok.Builder;
import lombok.Data;

@Data // Lombok
@Builder // Lombok para crear objetos de respuesta fácilmente
public class AuthResponse {

    private String token;
    private String email;
    private String role; // El rol principal del usuario
    private String tokenType = "Bearer";
}
