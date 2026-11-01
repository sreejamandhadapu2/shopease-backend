package com.shopease.backend.dto;

import java.time.LocalDateTime;

public class AddressResponse {

    private Long id;
    private String fullName;
    private String phoneNumber;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private LocalDateTime createdAt;

    public AddressResponse(Long id, String fullName, String phoneNumber,
                           String addressLine, String city, String state,
                           String pincode, String country,
                           LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.addressLine = addressLine;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPincode() {
        return pincode;
    }

    public String getCountry() {
        return country;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}