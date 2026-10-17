package com.example.morse.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9_-]{3,32}", message = "Use 3–32 letters, numbers, underscores, or hyphens.")
        String username,
        @NotBlank
        @Size(min = 10, max = 72, message = "Use a password between 10 and 72 characters.")
        String password) { }
