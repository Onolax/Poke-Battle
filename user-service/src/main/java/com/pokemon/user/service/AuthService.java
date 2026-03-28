package com.pokemon.user.service;

import com.pokemon.user.domain.User;
import com.pokemon.user.dto.LoginRequest;
import com.pokemon.user.dto.LoginResponse;
import com.pokemon.user.dto.RegisterRequest;
import com.pokemon.user.dto.RegisterResponse;
import com.pokemon.user.exception.BadCredentialsException;
import com.pokemon.user.exception.UsernameAlreadyExistsException;
import com.pokemon.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public RegisterResponse register(RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new UsernameAlreadyExistsException(req.username());
        }
        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        User saved = userRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getUsername());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(BadCredentialsException::new);
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException();
        }
        return new LoginResponse(jwtUtil.generate(user.getId(), user.getUsername()));
    }
}
