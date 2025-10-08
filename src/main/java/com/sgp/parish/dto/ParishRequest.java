package com.sgp.parish.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // 👈 Añadir esta para que Jackson pueda construir la instancia.
@AllArgsConstructor // 👈 Añadir esta, es necesaria si usas @Builder
public class ParishRequest {

    @NotBlank(message = "El nombre de la parroquia es obligatorio")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    private String address;

    @Size(max = 15)
    private String phone;

    @Email(message = "Debe ser un formato de correo electrónico válido")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String city;
}