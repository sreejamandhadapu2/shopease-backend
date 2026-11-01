package com.shopease.backend.controller;

import com.shopease.backend.dto.AddressRequest;
import com.shopease.backend.dto.AddressResponse;
import com.shopease.backend.service.AddressService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {

        String email = authentication.getName();

        AddressResponse response =
                addressService.addAddress(email, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
            Authentication authentication) {

        String email = authentication.getName();

        List<AddressResponse> response =
                addressService.getAddresses(email);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        String email = authentication.getName();

        AddressResponse response =
                addressService.updateAddress(addressId, email, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {

        String email = authentication.getName();

        addressService.deleteAddress(addressId, email);

        return ResponseEntity.noContent().build();
    }
}