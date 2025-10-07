package com.sgp.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data // Lombok
@Builder // Lombok para crear objetos de respuesta fácilmente
public class AuthResponse {

    private String token;
    private String email;
    private Set<String> roles; // <-- CAMBIAR a plural y Set
    private String tokenType = "Bearer";
}
