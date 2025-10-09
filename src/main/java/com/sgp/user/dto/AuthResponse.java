package com.sgp.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data // Lombok
@Builder // Lombok para crear objetos de respuesta fácilmente
public class AuthResponse {

    private String token;
    private String email;
    // Cambiar de String 'role' a Set<String> 'roles'
    private Set<String> roles; // El rol principal o el conjunto de roles del usuario

    @Builder.Default
    private String tokenType = "Bearer";
}
