
package com.shopease.backend.dto;

public class LoginResponse {

    private String token;
    private String tokenType;
    private UserResponse user;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            String tokenType,
            UserResponse user) {
        this.token = token;
        this.tokenType = tokenType;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
