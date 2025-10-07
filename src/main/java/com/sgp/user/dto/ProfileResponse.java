package com.sgp.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String address;
    private String phone;
}