
package com.shopease.backend.service;

import com.shopease.backend.dto.LoginRequest;
import com.shopease.backend.dto.LoginResponse;
import com.shopease.backend.dto.RegisterRequest;
import com.shopease.backend.dto.UpdateProfileRequest;
import com.shopease.backend.dto.UserResponse;
import com.shopease.backend.entity.User;
import com.shopease.backend.exception.EmailAlreadyExistsException;
import com.shopease.backend.exception.InvalidCredentialsException;
import com.shopease.backend.repository.UserRepository;
import com.shopease.backend.security.JwtService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,JwtService jwtService) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public User registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                "Email is already registered"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );

        // The entity's default role remains CUSTOMER.
        return userRepository.save(user);
    }
    public UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }

public LoginResponse loginUser(LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new InvalidCredentialsException(
                            "Invalid email or password"
                    ));

    boolean passwordMatches = passwordEncoder.matches(
            request.getPassword(),
            user.getPassword()
    );

    if (!passwordMatches) {
        throw new InvalidCredentialsException(
                "Invalid email or password"
        );
    }

    // Generate a JWT after successful authentication
    String token = jwtService.generateToken(user);

    // Return the token and safe user details
    return new LoginResponse(
            token,
            "Bearer",
            toUserResponse(user)
    );
}
@Transactional(readOnly = true)
public UserResponse getUserProfile(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    return toUserResponse(user);
}
@Transactional
public UserResponse updateUserProfile(
        String currentEmail,
        UpdateProfileRequest request) {

    User user = userRepository.findByEmail(currentEmail)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    // Check whether another user already owns the new email
    if (!user.getEmail().equalsIgnoreCase(request.getEmail())
            && userRepository.existsByEmail(request.getEmail())) {

        throw new EmailAlreadyExistsException(
                "Email is already registered");
    }

    user.setName(request.getName());
    user.setEmail(request.getEmail());

    User updatedUser = userRepository.save(user);

    return toUserResponse(updatedUser);
}

}
