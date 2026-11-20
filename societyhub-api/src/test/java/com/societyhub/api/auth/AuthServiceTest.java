package com.societyhub.api.auth;

import com.societyhub.api.auth.dto.AuthResponse;
import com.societyhub.api.auth.dto.RegisterRequest;
import com.societyhub.api.college.College;
import com.societyhub.api.college.CollegeRepository;
import com.societyhub.api.config.JwtService;
import com.societyhub.api.user.Role;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private CollegeRepository collegeRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    private RegisterRequest request(Role requestedRole) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("  Student Name  ");
        request.setEmail("  STUDENT@COLLEGE.EDU ");
        request.setPassword("password123");
        request.setRole(requestedRole);
        return request;
    }

    @Test
    void publicRegistrationAlwaysCreatesAStudentEvenIfAdminRoleIsRequested() {
        College college = mock(College.class);
        when(userRepository.existsByEmail("student@college.edu")).thenReturn(false);
        when(collegeRepository.findByEmailDomainIgnoreCase("college.edu")).thenReturn(Optional.of(college));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("test-token");

        AuthResponse response = authService.register(request(Role.COLLEGE_ADMIN));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals(Role.STUDENT, saved.getValue().getRole());
        assertSame(college, saved.getValue().getCollege());
        assertEquals("student@college.edu", saved.getValue().getEmail());
        assertEquals("Student Name", saved.getValue().getFullName());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals("test-token", response.getToken());
    }

    @Test
    void unmatchedEmailDomainLeavesTheAccountUnassigned() {
        when(userRepository.existsByEmail("student@unknown.edu")).thenReturn(false);
        when(collegeRepository.findByEmailDomainIgnoreCase("unknown.edu")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("test-token");

        RegisterRequest request = request(null);
        request.setEmail("student@unknown.edu");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertNull(saved.getValue().getCollege());
        assertEquals(Role.STUDENT, response.getRole());
    }

    @Test
    void duplicateEmailIsRejectedBeforeCreatingAnAccount() {
        when(userRepository.existsByEmail("student@college.edu")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.register(request(Role.PLATFORM_ADMIN)));
        verify(userRepository, never()).save(any(User.class));
    }
}