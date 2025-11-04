package com.example.notificationservice.configuration;

import com.example.notificationservice.business.client.PropertyServiceClient;
import com.example.notificationservice.business.client.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


@Slf4j
@Configuration
public class RestClientConfiguration {

    @Value("${app.services.property-service.url}")
    private String propertyServiceUrl;

    @Value("${app.services.user-service.url}")
    private String userServiceUrl;

    /**
     * Create RestClient with JWT token forwarding interceptor
     */
    private RestClient createRestClientWithAuth(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory())
                .requestInterceptor((request, body, execution) -> {
                    // Extract JWT token from SecurityContext
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                    if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                        Jwt jwt = (Jwt) authentication.getPrincipal();

                        // Add Authorization header with Bearer token
                        request.getHeaders().setBearerAuth(jwt.getTokenValue());

                        log.debug("✅ Added JWT token to request: {} {}",
                                request.getMethod(),
                                request.getURI());
                    } else {
                        log.warn("⚠️ No JWT token found in SecurityContext for request: {} {}",
                                request.getMethod(),
                                request.getURI());
                    }

                    return execution.execute(request, body);
                })
                .build();
    }

    /**
     * Property Service REST Client Bean
     */
    @Bean
    public PropertyServiceClient propertyServiceClient() {
        log.info("Creating PropertyServiceClient with base URL: {}", propertyServiceUrl);

        // Create RestClient with JWT forwarding
        RestClient restClient = createRestClientWithAuth(propertyServiceUrl);

        // Create adapter for HTTP Interface
        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // Create proxy factory
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        // Create and return the client proxy
        return factory.createClient(PropertyServiceClient.class);
    }

    /**
     * User Service REST Client Bean
     */
    @Bean
    public UserServiceClient userServiceClient() {
        log.info("Creating UserServiceClient with base URL: {}", userServiceUrl);

        // Create RestClient with JWT forwarding
        RestClient restClient = createRestClientWithAuth(userServiceUrl);

        // Create adapter for HTTP Interface
        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // Create proxy factory
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        // Create and return the client proxy
        return factory.createClient(UserServiceClient.class);
    }
}