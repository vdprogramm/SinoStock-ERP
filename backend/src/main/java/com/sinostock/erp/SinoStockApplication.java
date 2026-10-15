package com.sinostock.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Supports PostgreSQL URLs created by Render (postgresql://user:pass@host/db)
 * while keeping Spring Boot's normal JDBC/H2 configuration for local tests.
 */
@SpringBootApplication
public class SinoStockApplication {
    public static void main(String[] args) {
        configureHostedDatabase();
        SpringApplication.run(SinoStockApplication.class, args);
    }

    static void configureHostedDatabase() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) return;
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            System.setProperty("spring.datasource.url", databaseUrl);
            return;
        }
        URI uri = URI.create(databaseUrl);
        if (!"postgresql".equals(uri.getScheme()) && !"postgres".equals(uri.getScheme())) {
            throw new IllegalArgumentException("DATABASE_URL must be a PostgreSQL connection URL");
        }
        if (uri.getHost() == null || uri.getRawUserInfo() == null || uri.getRawPath() == null) {
            throw new IllegalArgumentException("DATABASE_URL is missing host, credentials, or database name");
        }
        String[] credentials = uri.getRawUserInfo().split(":", 2);
        if (credentials.length != 2) {
            throw new IllegalArgumentException("DATABASE_URL must contain username and password");
        }
        String username = URLDecoder.decode(credentials[0], StandardCharsets.UTF_8);
        String password = URLDecoder.decode(credentials[1], StandardCharsets.UTF_8);
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + ":" + (uri.getPort() == -1 ? 5432 : uri.getPort())
                + uri.getRawPath()
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        System.setProperty("spring.datasource.url", jdbcUrl);
        System.setProperty("spring.datasource.username", username);
        System.setProperty("spring.datasource.password", password);
    }
}
