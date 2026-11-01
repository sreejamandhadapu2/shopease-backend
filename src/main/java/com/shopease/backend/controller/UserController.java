package com.shopease.backend.controller;

import com.shopease.backend.dto.UpdateProfileRequest;
import com.shopease.backend.dto.UserResponse;
import com.shopease.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // View logged-in user's profile
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile(
            Authentication authentication) {

        String email = authentication.getName();

        UserResponse response =
                userService.getUserProfile(email);

        return ResponseEntity.ok(response);
    }

    // Update logged-in user's profile
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        String currentEmail = authentication.getName();

        UserResponse response =
                userService.updateUserProfile(currentEmail, request);

        return ResponseEntity.ok(response);
    }
}