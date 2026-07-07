package com.kimhong.job_portal.controller;


import com.kimhong.job_portal.dto.UserRequest;
import com.kimhong.job_portal.dto.UserResponse;
import com.kimhong.job_portal.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Users endpoint")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary="Admin views all users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    @Operation(summary="User views their individual's info")
    public  ResponseEntity<UserResponse> getUserInfo(Authentication authentication){
        return ResponseEntity.ok(userService.getMyInfo(authentication.getName()));
    }

    // For Admin
    @GetMapping("/{id}")
    @Operation(summary="Admin views user by user's id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/me")
    @Operation(summary="User updates their individual's info")
    public ResponseEntity<UserResponse> updateMyInfo(@Valid @RequestBody UserRequest request, Authentication authentication){
        return ResponseEntity.ok(userService.updateMyInfo(authentication.getName(),request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Admin delete any user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }



}
