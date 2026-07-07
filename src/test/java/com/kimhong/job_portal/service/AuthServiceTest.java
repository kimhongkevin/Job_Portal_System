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

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private final String mockToken = "mocked-jwt-token";

    @BeforeEach
    void setUp(){
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFullName("John Doe");
        sampleUser.setEmail("joh.doe@example.com");
        sampleUser.setPassword("encodedPassword123");
        sampleUser.setRole(Role.JOB_SEEKER);
    }

    @Nested
    @DisplayName("Register method test")
    class RegisterTests{

        @Test
        @DisplayName("Should successfully register a new user")
        void register_Success(){

            // Arrange
            RegisterRequest request = new RegisterRequest("John Doe","joh.doe@example.com","encodedPassword123",Role.JOB_SEEKER);

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtUtil.generateToken(request.getEmail(),request.getRole().name())).thenReturn(mockToken);

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertNotNull(response);
            assertEquals(mockToken,response.getToken());
            assertEquals(request.getEmail(),response.getEmail());
            assertEquals(request.getRole(),response.getRole());

            verify(userRepository, times(1)).existsByEmail(request.getEmail());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void register_ThrowsException_WhenEmailExists(){
            // Arrange
            RegisterRequest request = new RegisterRequest("John Doe","john.doe@example.com","encodedPassword123",Role.JOB_SEEKER);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // Act & Assert

            assertThrows(DuplicateResourceException.class, ()-> authService.register(request));

            // Ensure data saving and token generation are never reached
            verify(userRepository,never()).save(any(User.class));
            verify(jwtUtil,never()).generateToken(anyString(),anyString());
        }
    }

    @Nested
    @DisplayName("Login method test")
    class LoginTests{

        @Test
        @DisplayName("Should successfully login a user")
        void login_Success(){

            // Arrange
            LoginRequest request = new LoginRequest("john.doe@example.com","encodedPassword123");

            // Mock authenticationManager.authenticate returning successfully (not throwing an exception)
            when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(),request.getPassword()
            ))).thenReturn(null);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));
            when(jwtUtil.generateToken(sampleUser.getEmail(),sampleUser.getRole().name())).thenReturn(mockToken);

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            assertNotNull(response);
            assertEquals(mockToken,response.getToken());
            assertEquals(request.getEmail(),response.getEmail());
            assertEquals(sampleUser.getRole(),response.getRole());

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, times(1)).findByEmail(request.getEmail());
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when authentication fails")
        void login_ThrowsException_WhenCredentialsInvalid(){
            // Arrange
            LoginRequest request = new LoginRequest("john.doe@example.com","encodedPassword123");

            // Act & Assert
            assertThrows(BadCredentialsException.class, ()-> authService.login(request));

            // Verify userRepository and jwtUtil are never touched if authentication fails
            verify(userRepository,never()).save(any(User.class));
            verify(jwtUtil,never()).generateToken(anyString(),anyString());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user is not found after authentication")
        void login_ThrowsException_WhenUserNotFound(){
            // Arrange
            LoginRequest request = new LoginRequest("nonexisting.user@com","password123");

            when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword())))
                    .thenReturn(null);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, ()-> authService.login(request));

            verify(jwtUtil,never()).generateToken(anyString(),anyString());
        }
    }
}
