package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.AuthResponse;
import com.kimhong.job_portal.dto.LoginRequest;
import com.kimhong.job_portal.dto.RegisterRequest;
import com.kimhong.job_portal.dto.ResetPasswordRequest;
import com.kimhong.job_portal.entity.Role;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.UserRepository;
import com.kimhong.job_portal.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
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
        @DisplayName("Should successfully register a new JOB_SEEKER and send a welcome email")
        void register_Success() {
            // Arrange — RegisterRequest has no role field anymore (Option B)
            RegisterRequest request = new RegisterRequest("John Doe", "john.doe@example.com", "password123");

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(mockToken);

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertNotNull(response);
            assertEquals(mockToken, response.getToken());
            assertEquals(request.getEmail(), response.getEmail());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            // Self-registration always creates a JOB_SEEKER account
            assertEquals(Role.JOB_SEEKER, userCaptor.getValue().getRole());

            verify(emailService, times(1)).sendWelcomeEmail(sampleUser.getEmail(), sampleUser.getFullName());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException and never send an email if user exists")
        void register_ThrowsException_WhenEmailExists() {
            // Arrange
            RegisterRequest request = new RegisterRequest("John Doe", "john.doe@example.com", "password123");
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

    @Nested
    @DisplayName("Forgot Password Method Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should generate token with 15 minute expiry and send reset email")
        void forgotPassword_Success() {
            when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.forgotPassword(sampleUser.getEmail());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertNotNull(saved.getResetToken());
            assertFalse(saved.getResetToken().isBlank());
            assertNotNull(saved.getResetTokenExpiry());
            assertTrue(saved.getResetTokenExpiry().isAfter(LocalDateTime.now().plusMinutes(14)));

            verify(emailService, times(1)).sendPasswordResetEmail(
                    eq(sampleUser.getEmail()), eq(sampleUser.getFullName()), contains(saved.getResetToken()));
        }

        @Test
        @DisplayName("Should do nothing silently when email is unknown (no user enumeration)")
        void forgotPassword_DoesNothing_WhenEmailUnknown() {
            when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> authService.forgotPassword("ghost@example.com"));
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Reset Password Method Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should update password and clear the token on success")
        void resetPassword_Success() {
            sampleUser.setResetToken("valid-token");
            sampleUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));

            ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPassword123");

            when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> authService.resetPassword(request));

            assertEquals("encodedNewPassword", sampleUser.getPassword());
            assertNull(sampleUser.getResetToken());
            assertNull(sampleUser.getResetTokenExpiry());
            verify(userRepository, times(1)).save(sampleUser);
        }

        @Test
        @DisplayName("Should throw BadRequestException when token does not exist")
        void resetPassword_ThrowsException_WhenTokenInvalid() {
            when(userRepository.findByResetToken("bad-token")).thenReturn(Optional.empty());

            ResetPasswordRequest request = new ResetPasswordRequest("bad-token", "newPassword123");

            assertThrows(BadRequestException.class, () -> authService.resetPassword(request));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when token is expired")
        void resetPassword_ThrowsException_WhenTokenExpired() {
            sampleUser.setResetToken("expired-token");
            sampleUser.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1));

            when(userRepository.findByResetToken("expired-token")).thenReturn(Optional.of(sampleUser));

            ResetPasswordRequest request = new ResetPasswordRequest("expired-token", "newPassword123");

            assertThrows(BadRequestException.class, () -> authService.resetPassword(request));
            verify(userRepository, never()).save(any(User.class));
        }
    }
}
