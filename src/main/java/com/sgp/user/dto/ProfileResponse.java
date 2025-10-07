package com.sgp.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ProfileResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles; // <-- CAMBIAR a plural y Set
    private String address;
    private String phone;
}