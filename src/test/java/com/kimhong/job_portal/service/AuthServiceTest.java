package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.AuthResponse;
import com.kimhong.job_portal.dto.LoginRequest;
import com.kimhong.job_portal.dto.RegisterRequest;
import com.kimhong.job_portal.entity.Role;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.UserRepository;
import com.kimhong.job_portal.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private final String mockToken = "mocked-jwt-token";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFullName("John Doe");
        sampleUser.setEmail("john.doe@example.com");
        sampleUser.setPassword("encodedPassword123");
        sampleUser.setRole(Role.JOB_SEEKER);
    }

    @Nested
    @DisplayName("Register Method Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user and send a welcome email")
        void register_Success() {
            // Arrange
            RegisterRequest request = new RegisterRequest("John Doe", "john.doe@example.com", "password123", Role.JOB_SEEKER);

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtUtil.generateToken(request.getEmail(), request.getRole().name())).thenReturn(mockToken);

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertNotNull(response);
            assertEquals(mockToken, response.getToken());
            assertEquals(request.getEmail(), response.getEmail());

            verify(userRepository, times(1)).save(any(User.class));
            // Verify that the welcome email notification triggered successfully
            verify(emailService, times(1)).sendWelcomeEmail(sampleUser.getEmail(), sampleUser.getFullName());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException and never send an email if user exists")
        void register_ThrowsException_WhenEmailExists() {
            // Arrange
            RegisterRequest request = new RegisterRequest("John Doe", "john.doe@example.com", "password123", Role.JOB_SEEKER);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // Act & Assert
            assertThrows(DuplicateResourceException.class, () -> authService.register(request));

            // Verify infrastructure was never reached
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Login Method Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login a user")
        void login_Success() {
            // Arrange
            LoginRequest request = new LoginRequest("john.doe@example.com", "encodedPassword123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));
            when(jwtUtil.generateToken(sampleUser.getEmail(), sampleUser.getRole().name())).thenReturn(mockToken);

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            assertNotNull(response);
            assertEquals(mockToken, response.getToken());
            verify(userRepository, times(1)).findByEmail(request.getEmail());
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when email or password doesn't match")
        void login_ThrowsException_WhenCredentialsInvalid() {
            LoginRequest request = new LoginRequest("fake@example.com", "password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.login(request));
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user record is missing")
        void login_ThrowsException_WhenUserNotFound() {
            LoginRequest request = new LoginRequest("nonexisting@com", "password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
        }
    }
}