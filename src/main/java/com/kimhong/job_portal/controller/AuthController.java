package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.AuthResponse;
import com.kimhong.job_portal.dto.LoginRequest;
import com.kimhong.job_portal.dto.RegisterRequest;
import com.kimhong.job_portal.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Authentication",description = "Register and login endpoints")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register as JOB_SEEKER,EMPLOYER,ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Successfully register"),
            @ApiResponse(responseCode = "409",description = "Email already exist"),
            @ApiResponse(responseCode = "400",description = "Invalid input")
    })
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Login as JOB_SEEKER,EMPLOYER,ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Successfully register"),
            @ApiResponse(responseCode = "400",description = "Invalid input")
    })
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
