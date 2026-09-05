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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;



@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.password-reset-url:http://localhost:3000/reset-password}")
    private String passwordResetUrl;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("User's Email Already Exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Self-registration is only for JOB_SEEKER.
        // ADMIN accounts are created manually in the DB (or by an existing ADMIN).
        user.setRole(Role.JOB_SEEKER);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(),user.getRole().name());

        AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole());

        emailService.sendWelcomeEmail(user.getEmail(),user.getFullName());

        return response;
    }

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        String token = jwtUtil.generateToken(user.getEmail(),user.getRole().name());

        return new AuthResponse(token, user.getEmail(), user.getRole());

    }

    // Step 1 of password reset: generate token and email a reset link
    public void forgotPassword(String email){
        // Silently ignore unknown emails so we don't leak which accounts exist
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null)
            return;

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String resetLink = passwordResetUrl + "?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
    }

    // Step 2 of password reset: validate token and update the password
    public void resetPassword(ResetPasswordRequest request){
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if(user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Invalid or expired reset token");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

}
