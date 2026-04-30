package com.agriculture.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private boolean success;
    private Long userId;
    private String username;
    private String fullName;
    private String role;
}
