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
                // Demo credentials are for local development and automated tests ONLY.
                // Render uses SEED_DEMO=false; NEVER create known-password accounts online.
                seedUser(users, encoder, "admin", "Admin@12345", Role.ADMIN);
                seedUser(users, encoder, "warehouse", "Warehouse@12345", Role.WAREHOUSE_MANAGER);
                seedUser(users, encoder, "sales", "SalesStaff@12345", Role.SALES_STAFF);
                seedUser(users, encoder, "accountant", "Accountant@12345", Role.ACCOUNTANT);
                seedUser(users, encoder, "purchasing", "Purchasing@12345", Role.PURCHASING_MANAGER);
                seedUser(users, encoder, "manager", "Manager@12345", Role.GENERAL_MANAGER);
                seedUser(users, encoder, "staff", "Staff@12345", Role.STAFF); // Legacy
            }
            if (!demoEnabled && (adminPassword == null || adminPassword.isBlank()) && users.count() == 0) {
                throw new IllegalStateException(
                        "No accounts exist. Set ADMIN_PASSWORD in deployment environment (12+ chars).");
            }

            if ((demoEnabled || sampleProductsEnabled) && products.count() == 0) {
                products.save(new Product("CN-001", "Chuột không dây", "Chuột không dây", new BigDecimal("59.00"), 25, 5));
                products.save(new Product("CN-002", "Bàn phím cơ", "Bàn phím cơ", new BigDecimal("199.00"), 8, 10));
                products.save(new Product("CN-003", "Cáp USB-C", "Cáp USB-C", new BigDecimal("19.90"), 60, 10));
            }
        };
    }
    private static void seedUser(UserRepository users, PasswordEncoder encoder, String username,
                                 String password, Role role) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(new UserAccount(username, encoder.encode(password), role));
        }
    }

}
