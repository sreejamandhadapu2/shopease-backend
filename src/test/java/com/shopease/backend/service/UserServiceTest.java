package com.shopease.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldRetrieveUserProfile() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setRole("CUSTOMER");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getUserProfile("john@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findByEmail("john@example.com");
    }
    
    @Test
    void shouldRejectProfileRetrievalForUnknownUser() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getUserProfile("unknown@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
    
    @Test
    void shouldUpdateUserProfile() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setRole("CUSTOMER");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("John Updated");
        request.setEmail("john.updated@example.com");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("john.updated@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserProfile(
                "john@example.com", request);

        assertThat(response.getName()).isEqualTo("John Updated");
        assertThat(response.getEmail()).isEqualTo("john.updated@example.com");

        verify(userRepository).save(user);
    }
    @Test
    void shouldRejectUpdatingToExistingEmail() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("John Updated");
        request.setEmail("existing@example.com");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("existing@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.updateUserProfile("john@example.com", request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(request);

        assertThat(savedUser.getName()).isEqualTo("John");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");

        verify(userRepository).save(any(User.class));
    }
    @Test
    void shouldRejectRegistrationWithExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.registerUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void shouldLoginUserSuccessfully() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setRole("CUSTOMER");

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        user.setPassword(encoder.encode("password123"));

        LoginRequest request = new LoginRequest(
                "john@example.com",
                "password123"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        LoginResponse response = userService.loginUser(request);

        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail())
                .isEqualTo("john@example.com");

        verify(jwtService).generateToken(user);
    }
    @Test
    void shouldRejectLoginWithUnknownEmail() {
        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "password123"
        );

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.loginUser(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, never()).generateToken(any());
    }
    @Test
    void shouldRejectLoginWithIncorrectPassword() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setRole("CUSTOMER");

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        user.setPassword(encoder.encode("correctPassword"));

        LoginRequest request = new LoginRequest(
                "john@example.com",
                "wrongPassword"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.loginUser(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, never()).generateToken(any());
    }
    
}

