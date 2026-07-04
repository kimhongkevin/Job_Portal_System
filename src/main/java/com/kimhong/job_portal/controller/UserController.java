package com.kimhong.job_portal.controller;


import com.kimhong.job_portal.dto.UserRequest;
import com.kimhong.job_portal.dto.UserResponse;
import com.kimhong.job_portal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public  ResponseEntity<UserResponse> getUserInfo(Authentication authentication){
        return ResponseEntity.ok(userService.getMyInfo(authentication.getName()));
    }

    // For Admin
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(@RequestBody UserRequest request, Authentication authentication){
        return ResponseEntity.ok(userService.updateMyInfo(authentication.getName(),request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }



}
