package com.sinostock.erp.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> List.of(
                new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role"))));
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/error", "/assets/**", "/vite.svg").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/**", "/api/purchasing/**")
                            .hasAnyRole("ADMIN", "PURCHASING_MANAGER")
                        .requestMatchers("/api/customers/**")
                            .hasAnyRole("ADMIN", "SALES_STAFF", "GENERAL_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/accounting/**").hasAnyRole("ADMIN", "ACCOUNTANT")
                        .requestMatchers("/api/accounting/**")
                            .hasAnyRole("ADMIN", "ACCOUNTANT", "GENERAL_MANAGER")
                        .requestMatchers("/api/reports/**")
                            .hasAnyRole("ADMIN", "GENERAL_MANAGER", "ACCOUNTANT")
                        .requestMatchers(HttpMethod.POST, "/api/products", "/api/products/**")
                            .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                            .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**")
                            .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                            .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/orders", "/api/orders/**")
                            .hasAnyRole("ADMIN", "SALES_STAFF", "STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/**")
                            .hasAnyRole("ADMIN", "SALES_STAFF", "STAFF")
                        .requestMatchers("/api/inventory/logs/**")
                            .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "GENERAL_MANAGER", "ACCOUNTANT", "PURCHASING_MANAGER")
                        .requestMatchers("/api/dashboard")
                            .hasAnyRole("ADMIN", "GENERAL_MANAGER", "ACCOUNTANT", "WAREHOUSE_MANAGER", "SALES_STAFF", "PURCHASING_MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                            .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SALES_STAFF", "PURCHASING_MANAGER", "GENERAL_MANAGER", "ACCOUNTANT", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                            .hasAnyRole("ADMIN", "SALES_STAFF", "GENERAL_MANAGER", "ACCOUNTANT", "STAFF")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.jwt-secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(key(secret)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${app.jwt-secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key(secret)));
    }

    private SecretKeySpec key(String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors-origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
