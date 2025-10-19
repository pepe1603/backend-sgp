package com.sgp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestResetDTO {
    @NotBlank
    @Email
    private String email;
}