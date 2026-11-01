
package com.shopease.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // Swagger and OpenAPI documentation - public access
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()

                // Anyone can register or log in
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                // Customers and admins can view products and categories
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products", "/api/products/**",
                    "/api/categories", "/api/categories/**"
                ).hasAnyRole("CUSTOMER", "ADMIN")

                // Only admins can modify products and categories
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products", "/api/products/**",
                    "/api/categories", "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products", "/api/products/**",
                    "/api/categories", "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/products", "/api/products/**",
                    "/api/categories", "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products", "/api/products/**",
                    "/api/categories", "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers("/api/cart", "/api/cart/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")

                .requestMatchers("/api/orders", "/api/orders/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")

                .requestMatchers("/api/payments/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")

                .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                // All remaining requests require authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
