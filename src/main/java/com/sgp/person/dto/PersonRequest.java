package com.sgp.person.dto;

import com.sgp.common.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String lastName;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate birthDate;

    @NotNull(message = "El género es obligatorio")
    private Gender gender;

    @Size(max = 20, message = "El número de teléfono no debe exceder los 20 caracteres")
    private String phoneNumber;

    @NotBlank(message = "El tipo de identificación es obligatorio")
    @Size(max = 50, message = "El tipo de identificación no debe exceder los 50 caracteres")
    private String identificationType;

    @NotBlank(message = "El número de identificación es obligatorio")
    @Size(max = 50, message = "El número de identificación no debe exceder los 50 caracteres")
    private String identificationNumber;

    @NotNull(message = "El ID de la parroquia es obligatorio")
    private Long parishId; // Usamos el ID para crear la relación
}