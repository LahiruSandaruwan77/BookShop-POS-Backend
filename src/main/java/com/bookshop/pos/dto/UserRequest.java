package com.bookshop.pos.dto;

import com.bookshop.pos.entity.AppUser;
import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(min = 3, max = 40) String username,
        @Size(max = 20) String phone,
        @NotNull AppUser.Role role,
        // Temporary password — required on create, ignored on update.
        @Size(min = 6, max = 60) String password
) {}
