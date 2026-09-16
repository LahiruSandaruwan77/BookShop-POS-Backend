package com.bookshop.pos.dto;

import com.bookshop.pos.entity.AppUser;
import java.time.LocalDateTime;

/** Note what's absent: passwordHash. Never send it out, even hashed. */
public record UserResponse(
        Long id, String name, String username, String phone,
        String role, boolean active, boolean mustChangePassword,
        LocalDateTime createdAt
) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getName(), u.getUsername(), u.getPhone(),
                u.getRole().name(), u.isActive(), u.isMustChangePassword(), u.getCreatedAt());
    }
}
