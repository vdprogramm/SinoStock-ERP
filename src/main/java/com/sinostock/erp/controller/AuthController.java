package com.sinostock.erp.controller;

import com.sinostock.erp.dto.ApiDtos.*;
import com.sinostock.erp.exception.ApiException;
import com.sinostock.erp.repository.UserRepository;
import com.sinostock.erp.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    public AuthController(UserRepository users, PasswordEncoder passwords, TokenService tokens) {
        this.users = users; this.passwords = passwords; this.tokens = tokens;
    }
    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var user = users.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng"));
        if (!passwords.matches(request.password(), user.getPasswordHash()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng");
        return new LoginResponse(tokens.issue(user), "Bearer", user.getUsername(), user.getRole());
    }
    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP", "application", "SinoStock ERP"); }
}
