package com.bookshop.pos.config;

import com.bookshop.pos.entity.*;
import com.bookshop.pos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Runs at startup; seeds a first admin and sample data ONLY if the DB is empty.
 * Without this you'd have a chicken-and-egg problem: no user exists to log in
 * and create users.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(AppUserRepository users, CategoryRepository categories,
                           ProductRepository products, StockMovementRepository movements,
                           PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) return; // already seeded

            users.save(new AppUser("Kavindu", "kavindu", null,
                    encoder.encode("admin123"), AppUser.Role.ADMIN));

            Category books = categories.save(new Category("Books"));
            Category stationery = categories.save(new Category("Stationery"));
            Category services = categories.save(new Category("Services"));

            Product pen = products.save(new Product("4713270000117", "Atlas Blue Pen",
                    stationery, new BigDecimal("28"), new BigDecimal("40"),
                    new BigDecimal("240"), false));
            Product book = products.save(new Product("9789556652109", "Madol Doova",
                    books, new BigDecimal("480"), new BigDecimal("650"),
                    new BigDecimal("12"), false));
            products.save(new Product(null, "Photocopy (per page)",
                    services, BigDecimal.ZERO, new BigDecimal("5"), null, true));

            movements.save(new StockMovement(pen, new BigDecimal("240"),
                    StockMovement.Reason.OPENING, "Opening stock"));
            movements.save(new StockMovement(book, new BigDecimal("12"),
                    StockMovement.Reason.OPENING, "Opening stock"));

            System.out.println(">>> Seeded first admin: kavindu / admin123 (change this!)");
        };
    }
}
