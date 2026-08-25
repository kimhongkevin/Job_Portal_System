package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.AuthResponse;
import com.kimhong.job_portal.dto.ForgotPasswordRequest;
import com.kimhong.job_portal.dto.LoginRequest;
import com.kimhong.job_portal.dto.RegisterRequest;
import com.kimhong.job_portal.dto.ResetPasswordRequest;
import com.kimhong.job_portal.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication",description = "Register, login and password reset endpoints")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Self-registration always creates a JOB_SEEKER account. " +
                    "ADMIN accounts are created manually in the database.")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Login as JOB_SEEKER or ADMIN")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email",
            description = "Generates a token valid for 15 minutes and emails a reset link")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the token from the email")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
