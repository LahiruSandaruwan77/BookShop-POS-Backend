package com.bookshop.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Real security, replacing the dev config:
 *  - Session-based login via POST /api/auth/login (see AuthController)
 *  - Roles enforced with @PreAuthorize on controllers (@EnableMethodSecurity)
 *  - Unauthenticated API calls get 401 JSON-style, not an HTML redirect
 *
 * CSRF note: disabled here. The classic CSRF attack needs a malicious site
 * reaching your server — this app binds to localhost on one shop PC that
 * shouldn't browse the web while running the POS. If you ever expose this
 * beyond localhost, enable CSRF with CookieCsrfTokenRepository and send the
 * token from React.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 console (dev)
            .authorizeHttpRequests(a -> a
                // Login itself must be open, or nobody could ever log in.
                .requestMatchers("/api/auth/login").permitAll()
                // Dev-only H2 console; remove before the shop goes live.
                .requestMatchers("/h2-console/**").permitAll()
                // The React app's static files (index.html, assets) are public;
                // the app itself redirects to the login page when /api/auth/me fails.
                .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                // Everything else needs a logged-in user. Admin-only endpoints
                // are further locked with @PreAuthorize("hasRole('ADMIN')").
                .anyRequest().authenticated()
            )
            // APIs should answer 401, never redirect to a login page.
            .exceptionHandling(e -> e.authenticationEntryPoint(
                (req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value(), "Not logged in")));
        return http.build();
    }
}
