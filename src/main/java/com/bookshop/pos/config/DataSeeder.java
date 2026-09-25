package com.bookshop.pos.config;

import com.bookshop.pos.entity.*;
import com.bookshop.pos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;


@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(AppUserRepository users, CategoryRepository categories,
                           ProductRepository products, StockMovementRepository movements,
                           PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) return; // already seeded

            users.save(new AppUser("ApexPOS", "admin", null,
                    encoder.encode("admin123"), AppUser.Role.ADMIN));

            System.out.println("Seeded first admin: admin / admin123 (change this!)");
        };
    }
}
