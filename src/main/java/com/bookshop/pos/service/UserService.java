package com.bookshop.pos.service;

import com.bookshop.pos.dto.UserRequest;
import com.bookshop.pos.entity.AppUser;
import com.bookshop.pos.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
public class UserService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public UserService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public AppUser create(UserRequest req) {
        String username = req.username().trim().toLowerCase();
        if (users.findByUsernameIgnoreCase(username).isPresent())
            throw new ResponseStatusException(CONFLICT, "Username already taken");
        if (req.password() == null || req.password().length() < 6)
            throw new ResponseStatusException(BAD_REQUEST, "Temporary password (min 6 chars) is required");

        AppUser u = new AppUser(req.name().trim(), username,
                blankToNull(req.phone()), encoder.encode(req.password()), req.role());
        u.setMustChangePassword(true); // forced to pick their own at first login
        return users.save(u);
    }

    @Transactional
    public AppUser update(Long id, UserRequest req, String actingUsername) {
        AppUser u = require(id);
        String username = req.username().trim().toLowerCase();
        users.findByUsernameIgnoreCase(username)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new ResponseStatusException(CONFLICT, "Username already taken"); });

        if (req.role() != u.getRole()) {
            if (u.getUsername().equals(actingUsername))
                throw new ResponseStatusException(CONFLICT, "You can't change your own role");
            if (isLastActiveAdmin(u))
                throw new ResponseStatusException(CONFLICT, "Can't demote the only active admin");
        }

        u.setName(req.name().trim());
        u.setUsername(username);
        u.setPhone(blankToNull(req.phone()));
        u.setRole(req.role());
        return u;
    }

    @Transactional
    public AppUser setActive(Long id, boolean active, String actingUsername) {
        AppUser u = require(id);
        if (!active) {
            if (u.getUsername().equals(actingUsername))
                throw new ResponseStatusException(CONFLICT, "You can't deactivate yourself");
            if (isLastActiveAdmin(u))
                throw new ResponseStatusException(CONFLICT, "Can't deactivate the only active admin");
        }
        u.setActive(active);
        return u;
    }

    /** Admin resets someone's password to a temporary one — never views it. */
    @Transactional
    public void resetPassword(Long id, String temporaryPassword) {
        AppUser u = require(id);
        u.setPasswordHash(encoder.encode(temporaryPassword));
        u.setMustChangePassword(true);
    }

    /** A user changes their OWN password (also clears the first-login flag). */
    @Transactional
    public void changeOwnPassword(String username, String currentPassword, String newPassword) {
        AppUser u = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unknown user"));
        if (!encoder.matches(currentPassword, u.getPasswordHash()))
            throw new ResponseStatusException(BAD_REQUEST, "Current password is incorrect");
        u.setPasswordHash(encoder.encode(newPassword));
        u.setMustChangePassword(false);
    }

    private AppUser require(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    private boolean isLastActiveAdmin(AppUser u) {
        return u.getRole() == AppUser.Role.ADMIN && u.isActive()
                && users.countByRoleAndActiveTrue(AppUser.Role.ADMIN) == 1;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
