package com.bookshop.pos.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 6, max = 60) String newPassword) {}
    public record ResetPasswordRequest(@NotBlank @Size(min = 6, max = 60) String temporaryPassword) {}
    public record MeResponse(String username, String name, String role, boolean mustChangePassword) {}
}
