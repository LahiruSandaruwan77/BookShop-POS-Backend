package com.bookshop.pos.config;

import com.bookshop.pos.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges OUR app_users table to Spring Security. When someone logs in,
 * Spring asks this class "who is 'kavindu'?" and then checks the BCrypt
 * hash itself — we never compare passwords manually.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public JpaUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.findByUsernameIgnoreCase(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPasswordHash())
                        .roles(u.getRole().name())   // becomes ROLE_ADMIN / ROLE_CASHIER
                        .disabled(!u.isActive())      // deactivated users can't log in
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
    }
}
