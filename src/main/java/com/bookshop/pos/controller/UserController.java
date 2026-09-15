package com.bookshop.pos.controller;

import com.bookshop.pos.dto.AuthDtos.ResetPasswordRequest;
import com.bookshop.pos.dto.UserRequest;
import com.bookshop.pos.dto.UserResponse;
import com.bookshop.pos.repository.AppUserRepository;
import com.bookshop.pos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')") 
public class UserController {

    private final UserService userService;
    private final AppUserRepository users;

    public UserController(UserService userService, AppUserRepository users) {
        this.userService = userService;
        this.users = users;
    }

    @GetMapping
    public List<UserResponse> all() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest req) {
        return UserResponse.from(userService.create(req));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest req,
                               Principal principal) {
        return UserResponse.from(userService.update(id, req, principal.getName()));
    }

    @PatchMapping("/{id}/active")
    public UserResponse setActive(@PathVariable Long id, @RequestParam boolean value,
                                  Principal principal) {
        return UserResponse.from(userService.setActive(id, value, principal.getName()));
    }

    @PostMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest req) {
        userService.resetPassword(id, req.temporaryPassword());
    }
}
