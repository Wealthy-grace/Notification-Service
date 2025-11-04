package com.example.notificationservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/notifications/confirm-by-token/**").permitAll()

                        // All appointment endpoints require authentication
                        // Fine-grained authorization is handled by AppointmentSecurityService
                        .requestMatchers("/api/appointments/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );



        return http.build();
    }


    // JWT Authentication Converter
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());

        // Use preferred_username as the principal name (Keycloak default)
        converter.setPrincipalClaimName("preferred_username");

        return converter;
    }

    //  Extracts authorities/roles from Keycloak JWT token
    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Extract roles from realm_access claim
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");

                // Map each role to an authority with correct prefix handling
                authorities = roles.stream()
                        .map(this::mapRoleToAuthority)
                        .collect(Collectors.toList());
            }

            return authorities;
        };
    }


    // Map a Keycloak role to a Spring Security GrantedAuthority
    // ADMIN" → "ROLE_ADMIN" (adds prefix
    private GrantedAuthority mapRoleToAuthority(String role) {
        // If role already starts with "ROLE_", use it as-is
        if (role.startsWith("ROLE_")) {
            return new SimpleGrantedAuthority(role);
        }

        // Standard Keycloak internal roles - use as-is without ROLE_ prefix
        if (role.startsWith("default-roles-") ||
                role.equals("offline_access") ||
                role.equals("uma_authorization")) {
            return new SimpleGrantedAuthority(role);
        }

        // Custom application roles - add ROLE_ prefix
        return new SimpleGrantedAuthority("ROLE_" + role);
    }
}



//// NEW TODO:
//package com.example.notificationservice.configuration;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
//import org.springframework.security.web.SecurityFilterChain;
//
/////
////  Security Configuration
////  Configures OAuth2 Resource Server with Keycloak JWT authentication
////
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//@RequiredArgsConstructor
//public class SecurityConfiguration {
//
//    // Configure security filter chain
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth
//                        // Public endpoints
//                        .requestMatchers(
//                                "/actuator/**",
//                                "/api-docs/**",
//                                "/swagger-ui/**",
//                                "/swagger-ui.html",
//                                "/v3/api-docs/**"
//                        ).permitAll()
//
//                        // Internal endpoints (from other services)
//                        .requestMatchers("/api/internal/**").permitAll()
//
//                        // Admin endpoints
//                        .requestMatchers(HttpMethod.DELETE, "/api/notifications/**").hasRole("ADMIN")
//                        .requestMatchers("/api/notifications/statistics").hasRole("ADMIN")
//                        .requestMatchers("/api/notifications/retry-failed").hasRole("ADMIN")
//
//                        // Authenticated endpoints
//                        .requestMatchers("/api/notifications/**").authenticated()
//
//                        // Any other request requires authentication
//                        .anyRequest().authenticated()
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
//                        )
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                );
//
//        return http.build();
//    }
//
////     Configure JWT authentication converter
////      Extracts roles from Keycloak JWT token
//
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
//        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
//
//        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
//
//        return jwtAuthenticationConverter;
//    }
//}
//
