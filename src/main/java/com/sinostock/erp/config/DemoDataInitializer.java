package com.sinostock.erp.config;

import com.sinostock.erp.domain.*;
import com.sinostock.erp.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DemoDataInitializer {
    @Bean
    CommandLineRunner seedDemo(
            UserRepository users,
            ProductRepository products,
            PasswordEncoder encoder,
            @Value("${app.seed-demo:false}") boolean demoEnabled,
            @Value("${app.seed-sample-products:false}") boolean sampleProductsEnabled,
            @Value("${app.bootstrap-admin-username:admin}") String adminUsername,
            @Value("${app.bootstrap-admin-password:}") String adminPassword) {

        return args -> {
            // Public deployment: provision a one-time administrator using a Render secret.
            if (adminPassword != null && !adminPassword.isBlank()) {
                if (adminPassword.length() < 12) {
                    throw new IllegalArgumentException("ADMIN_PASSWORD must have at least 12 characters");
                }
                if (adminUsername == null || adminUsername.isBlank()) {
                    throw new IllegalArgumentException("ADMIN_USERNAME is required");
                }
                if (users.findByUsername(adminUsername).isEmpty()) {
                    users.save(new UserAccount(adminUsername, encoder.encode(adminPassword), Role.ADMIN));
                }
                // Do NOT reset an existing account's password on every application restart.
            }

            if (demoEnabled) {
                if (users.findByUsername("admin").isEmpty()) {
                    users.save(new UserAccount("admin", encoder.encode("Admin@12345"), Role.ADMIN));
                }
                if (users.findByUsername("staff").isEmpty()) {
                    users.save(new UserAccount("staff", encoder.encode("Staff@12345"), Role.STAFF));
                }
            }
            if (!demoEnabled && (adminPassword == null || adminPassword.isBlank()) && users.count() == 0) {
                throw new IllegalStateException(
                        "No accounts exist. Set ADMIN_PASSWORD in deployment environment (12+ chars).");
            }

            if ((demoEnabled || sampleProductsEnabled) && products.count() == 0) {
                products.save(new Product("CN-001", "无线鼠标", "Wireless Mouse", new BigDecimal("59.00"), 25, 5));
                products.save(new Product("CN-002", "机械键盘", "Mechanical Keyboard", new BigDecimal("199.00"), 8, 10));
                products.save(new Product("CN-003", "USB-C数据线", "USB-C Cable", new BigDecimal("19.90"), 60, 10));
            }
        };
    }
}
