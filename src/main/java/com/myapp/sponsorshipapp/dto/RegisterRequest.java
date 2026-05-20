package com.myapp.sponsorshipapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 3, message = "Username must be at least 3 characters")
    private String name;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*@).{6,}$",
            message = "Password must include letters, numbers, and @"
    )
    private String password;
    
    @NotBlank(message = "Role is required")
    private String role; // BRAND, INFLUENCER
}

