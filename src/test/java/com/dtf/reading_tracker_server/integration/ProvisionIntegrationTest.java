package com.dtf.reading_tracker_server.integration;

import com.dtf.reading_tracker_server.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ProvisionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "https://auth.example.test/");
        registry.add("okta.oauth2.issuer", () -> "https://auth.example.test/");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void provisionCreatesUserFromAuthenticatedJwtAndIsIdempotent() throws Exception {
        mockMvc.perform(post("/api/auth/provision")
                        .with(jwt().jwt(token -> token
                                .subject("auth0|integration-user")
                                .claim("email", "integration@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        mockMvc.perform(post("/api/auth/provision")
                        .with(jwt().jwt(token -> token
                                .subject("auth0|integration-user")
                                .claim("email", "changed@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.findByAuthId("auth0|integration-user"))
                .isPresent()
                .get()
                .satisfies(user -> assertThat(user.getEmail()).isEqualTo("integration@example.com"));
    }
}
