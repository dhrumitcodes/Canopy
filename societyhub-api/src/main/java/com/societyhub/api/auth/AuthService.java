package com.societyhub.api.auth;

import com.societyhub.api.auth.dto.AuthResponse;
import com.societyhub.api.auth.dto.LoginRequest;
import com.societyhub.api.auth.dto.RegisterRequest;
import com.societyhub.api.college.CollegeRepository;
import com.societyhub.api.config.JwtService;
import com.societyhub.api.user.Role;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String domain = email.substring(email.lastIndexOf('@') + 1);
        var college = collegeRepository.findByEmailDomainIgnoreCase(domain).orElse(null);

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .college(college)
                .build();

        userRepository.save(user);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}