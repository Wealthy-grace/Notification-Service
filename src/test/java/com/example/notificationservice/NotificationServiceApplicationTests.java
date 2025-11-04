package com.example.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
// Disable OAuth2 resource server auto-configuration for context loading
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/fake-jwk-set",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/fake-issuer"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Just verifies that the Spring context starts properly under the test profile
    }
}
