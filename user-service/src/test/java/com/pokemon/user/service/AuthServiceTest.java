package com.pokemon.user.service;

import com.pokemon.user.domain.User;
import com.pokemon.user.dto.LoginRequest;
import com.pokemon.user.dto.LoginResponse;
import com.pokemon.user.dto.RegisterRequest;
import com.pokemon.user.dto.RegisterResponse;
import com.pokemon.user.exception.BadCredentialsException;
import com.pokemon.user.exception.UsernameAlreadyExistsException;
import com.pokemon.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void register_savesHashedPassword() {
        when(userRepository.findByUsername("ash")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pikachu")).thenReturn("hashed");

        User saved = new User();
        saved.setId(UUID.randomUUID());
        saved.setUsername("ash");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterResponse resp = authService.register(new RegisterRequest("ash", "ash@pallet.com", "pikachu"));

        verify(passwordEncoder).encode("pikachu");
        verify(userRepository).save(any(User.class));
        assertThat(resp.username()).isEqualTo("ash");
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("ash");
        user.setPasswordHash("hashed");

        when(userRepository.findByUsername("ash")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pikachu", "hashed")).thenReturn(true);
        when(jwtUtil.generate(user.getId(), "ash")).thenReturn("token123");

        LoginResponse resp = authService.login(new LoginRequest("ash", "pikachu"));
        assertThat(resp.token()).isEqualTo("token123");
    }

    @Test
    void login_wrongPassword_throws() {
        User user = new User();
        user.setPasswordHash("hashed");
        when(userRepository.findByUsername("ash")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ash", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_unknownUser_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "pw")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
