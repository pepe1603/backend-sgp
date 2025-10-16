package com.sgp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyCodeDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank @Size(min = 6, max = 6)
    private String code;
}