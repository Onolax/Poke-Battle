package com.pokemon.user.controller;

import com.pokemon.user.dto.LoginRequest;
import com.pokemon.user.dto.LoginResponse;
import com.pokemon.user.dto.RegisterRequest;
import com.pokemon.user.dto.RegisterResponse;
import com.pokemon.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
