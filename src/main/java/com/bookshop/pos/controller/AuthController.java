package com.bookshop.pos.controller;

import com.bookshop.pos.dto.AuthDtos.*;
import com.bookshop.pos.entity.AppUser;
import com.bookshop.pos.repository.AppUserRepository;
import com.bookshop.pos.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final AppUserRepository users;
    private final UserService userService;
    private final SecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authManager, AppUserRepository users,
                          UserService userService) {
        this.authManager = authManager;
        this.users = users;
        this.userService = userService;
    }

   
    @PostMapping("/login")
    public MeResponse login(@Valid @RequestBody LoginRequest req,
                            HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.username().trim().toLowerCase(), req.password()));

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        contextRepo.saveContext(ctx, request, response);

        return me(auth::getName);
    }

    
    @GetMapping("/me")
    public MeResponse me(Principal principal) {
        if (principal == null) throw new ResponseStatusException(UNAUTHORIZED, "Not logged in");
        AppUser u = users.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unknown user"));
        return new MeResponse(u.getUsername(), u.getName(), u.getRole().name(),
                u.isMustChangePassword());
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req, Principal principal) {
        if (principal == null) throw new ResponseStatusException(UNAUTHORIZED, "Not logged in");
        userService.changeOwnPassword(principal.getName(),
                req.currentPassword(), req.newPassword());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }
}
