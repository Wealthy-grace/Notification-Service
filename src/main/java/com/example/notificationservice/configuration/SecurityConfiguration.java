//package com.example.notificationservice.configuration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
//import org.springframework.security.web.SecurityFilterChain;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//public class SecurityConfiguration {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                .authorizeHttpRequests(authz -> authz
//                        // Public endpoints
//                        // CRITICAL: Actuator endpoints for Kubernetes probes
//                        .requestMatchers("/actuator/health/**").permitAll()
//                        .requestMatchers("/actuator/health").permitAll()
//                        .requestMatchers("/actuator/info").permitAll()
//                        .requestMatchers("/actuator/metrics/**").permitAll()
//                        .requestMatchers("/actuator/prometheus").permitAll()
//                        //.requestMatchers("/actuator/health", "/actuator/info").permitAll()
//                        .requestMatchers("/api/notifications/confirm-by-token/**").permitAll()
//                        // Internal endpoints - allow service-to-service calls
//                        .requestMatchers("/api/notifications/new-property").permitAll()
//                        .requestMatchers("/api/notifications/booking-confirmation").permitAll()
//                        .requestMatchers("/api/notifications/appointment-reminder").permitAll()
//
//                        // All other notification endpoints require authentication
//                        .requestMatchers("/api/notifications/**").authenticated()
//
//                        // All appointment endpoints require authentication
//                        // Fine-grained authorization is handled by AppointmentSecurityService
//                        .requestMatchers("/api/appointments/**").authenticated()
//
//                        // All other requests require authentication
//                        .anyRequest().authenticated()
//                )
//
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
//                        )
//                );
//
//
//
//        return http.build();
//    }
//
//
//    // JWT Authentication Converter
//    @Bean
//    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
//        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
//
//        // Use preferred_username as the principal name (Keycloak default)
//        converter.setPrincipalClaimName("preferred_username");
//
//        return converter;
//    }
//
//    //  Extracts authorities/roles from Keycloak JWT token
//    @Bean
//    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
//        return jwt -> {
//            Collection<GrantedAuthority> authorities = new ArrayList<>();
//
//            // Extract roles from realm_access claim
//            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
//            if (realmAccess != null && realmAccess.containsKey("roles")) {
//                @SuppressWarnings("unchecked")
//                List<String> roles = (List<String>) realmAccess.get("roles");
//
//                // Map each role to an authority with correct prefix handling
//                authorities = roles.stream()
//                        .map(this::mapRoleToAuthority)
//                        .collect(Collectors.toList());
//            }
//
//            return authorities;
//        };
//    }
//
//
//    // Map a Keycloak role to a Spring Security GrantedAuthority
//    // ADMIN" → "ROLE_ADMIN" (adds prefix
//    private GrantedAuthority mapRoleToAuthority(String role) {
//        // If role already starts with "ROLE_", use it as-is
//        if (role.startsWith("ROLE_")) {
//            return new SimpleGrantedAuthority(role);
//        }
//
//        // Standard Keycloak internal roles - use as-is without ROLE_ prefix
//        if (role.startsWith("default-roles-") ||
//                role.equals("offline_access") ||
//                role.equals("uma_authorization")) {
//            return new SimpleGrantedAuthority(role);
//        }
//
//        // Custom application roles - add ROLE_ prefix
//        return new SimpleGrantedAuthority("ROLE_" + role);
//    }
//}
//
//
//


// TODO: Add Keycloak Security Configuration

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
                        // CRITICAL: Actuator endpoints for Kubernetes probes
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/metrics/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/api/notifications/confirm-by-token/**").permitAll()
                        // Internal endpoints - allow service-to-service calls
                        .requestMatchers("/api/notifications/new-property").permitAll()
                        .requestMatchers("/api/notifications/booking-confirmation").permitAll()
                        .requestMatchers("/api/notifications/appointment-reminder").permitAll()

                        // All other notification endpoints require authentication
                        .requestMatchers("/api/notifications/**").authenticated()

                        // All appointment endpoints require authentication
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

    // REMOVED @Bean annotation to prevent Spring from auto-registering it as a format converter!
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

    // REMOVED @Bean annotation to prevent Spring from auto-registering it as a format converter!
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Extract roles from realm_access claim
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");

                authorities = roles.stream()
                        .map(this::mapRoleToAuthority)
                        .collect(Collectors.toList());
            }

            return authorities;
        };
    }

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