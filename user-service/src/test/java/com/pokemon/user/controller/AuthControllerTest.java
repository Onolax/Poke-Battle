package com.pokemon.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.user.dto.LoginRequest;
import com.pokemon.user.dto.LoginResponse;
import com.pokemon.user.dto.RegisterRequest;
import com.pokemon.user.dto.RegisterResponse;
import com.pokemon.user.exception.BadCredentialsException;
import com.pokemon.user.exception.GlobalExceptionHandler;
import com.pokemon.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    @Test
    void register_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.register(any())).thenReturn(new RegisterResponse(userId, "ash"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest("ash", "ash@pallet.com", "pikachu"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("ash"));
    }

    @Test
    void login_returns200_withToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("test.jwt.token"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("ash", "pikachu"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void login_badPassword_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("ash", "wrong"))))
                .andExpect(status().isUnauthorized());
    }
}
